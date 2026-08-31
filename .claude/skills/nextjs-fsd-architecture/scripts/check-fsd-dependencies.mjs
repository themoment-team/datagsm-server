import { existsSync } from "node:fs";
import { readdir, readFile } from "node:fs/promises";
import { dirname, extname, relative, resolve, sep } from "node:path";

const layerOrder = ["shared", "entities", "features", "widgets", "views", "app"];
const layerRank = new Map(layerOrder.map((layer, index) => [layer, index]));
const sourceExtensions = new Set([".ts", ".tsx"]);
function isWordAt(source, index, word) {
    const before = source[index - 1];
    const after = source[index + word.length];
    return source.startsWith(word, index) && !/[\w$]/.test(before ?? "") && !/[\w$]/.test(after ?? "");
}

function skipTrivia(source, index) {
    while (index < source.length) {
        if (/\s/.test(source[index])) index += 1;
        else if (source.startsWith("//", index)) {
            index = source.indexOf("\n", index + 2);
            if (index === -1) return source.length;
        } else if (source.startsWith("/*", index)) {
            index = source.indexOf("*/", index + 2);
            if (index === -1) return source.length;
            index += 2;
        } else break;
    }
    return index;
}

function readString(source, index) {
    const quote = source[index];
    if (quote !== "'" && quote !== '"') return null;

    let end = index + 1;
    while (end < source.length) {
        if (source[end] === "\\") end += 2;
        else if (source[end] === quote) return { value: source.slice(index + 1, end), end: end + 1 };
        else end += 1;
    }
    return null;
}

function skipString(source, index) {
    const quote = source[index];
    let end = index + 1;
    while (end < source.length) {
        if (source[end] === "\\") end += 2;
        else if (source[end] === quote) return end + 1;
        else end += 1;
    }
    return end;
}

function staticSpecifier(source, index, isImport) {
    const first = skipTrivia(source, index);
    const direct = isImport && readString(source, first);
    if (direct) return direct.value;
    if (isImport && source[first] === ".") return null;

    for (let current = first; current < source.length; current += 1) {
        current = skipTrivia(source, current);
        if (source[current] === ";") return null;
        if (isWordAt(source, current, "from")) {
            return readString(source, skipTrivia(source, current + 4))?.value ?? null;
        }
        if (["'", '"', "`"].includes(source[current])) current = skipString(source, current) - 1;
    }
    return null;
}

function importSpecifiers(source) {
    const specifiers = [];
    for (let index = 0; index < source.length; index += 1) {
        index = skipTrivia(source, index);
        if (["'", '"', "`"].includes(source[index])) {
            index = skipString(source, index) - 1;
            continue;
        }
        const isImport = isWordAt(source, index, "import");
        const isExport = isWordAt(source, index, "export");
        if (!isImport && !isExport) continue;

        const start = skipTrivia(source, index + 6);
        if (isImport && source[start] === "(") {
            const dynamic = readString(source, skipTrivia(source, start + 1));
            if (dynamic) specifiers.push(dynamic.value);
        } else {
            const specifier = staticSpecifier(source, start, isImport);
            if (specifier) specifiers.push(specifier);
        }
        index = start - 1;
    }
    return specifiers;
}

async function sourceFiles(path) {
    const entries = await readdir(path, { withFileTypes: true });
    const files = await Promise.all(
        entries.map(async (entry) => {
            const entryPath = resolve(path, entry.name);
            if (entry.isDirectory()) return sourceFiles(entryPath);
            return sourceExtensions.has(extname(entry.name)) ? [entryPath] : [];
        }),
    );
    return files.flat();
}

function sliceInfo(sourceRoot, path) {
    const parts = relative(sourceRoot, path).split(sep);
    const [layer, slice] = parts;
    return layerRank.has(layer) ? { layer, slice } : null;
}

function importedPath(sourceRoot, file, specifier) {
    if (specifier.startsWith("@/")) return resolve(sourceRoot, specifier.slice(2));
    if (specifier.startsWith("./") || specifier.startsWith("../")) return resolve(dirname(file), specifier);
    return null;
}

function isEntityCrossImport(sourceRoot, source, target, targetInfo) {
    if (source.layer !== "entities" || targetInfo.layer !== "entities" || !source.slice) return false;

    const [layer, slice, marker, consumer] = relative(sourceRoot, target).split(sep);
    return layer === "entities" && slice === targetInfo.slice && marker === "@x" && consumer === source.slice;
}

async function checkSourceRoot(sourceRoot) {
    const violations = [];
    for (const file of await sourceFiles(sourceRoot)) {
        const source = sliceInfo(sourceRoot, file);
        if (!source) continue;

        const content = await readFile(file, "utf8");
        for (const specifier of importSpecifiers(content)) {
            const target = importedPath(sourceRoot, file, specifier);
            const targetInfo = target && sliceInfo(sourceRoot, target);
            if (!targetInfo) continue;

            if (layerRank.get(targetInfo.layer) > layerRank.get(source.layer)) {
                violations.push(`${relative(sourceRoot, file)}: ${source.layer} cannot import ${targetInfo.layer}`);
            }

            const isBusinessLayer = !["app", "shared"].includes(source.layer);
            if (
                isBusinessLayer
                && source.layer === targetInfo.layer
                && source.slice !== targetInfo.slice
                && !isEntityCrossImport(sourceRoot, source, target, targetInfo)
            ) {
                violations.push(`${relative(sourceRoot, file)}: ${source.layer} slices cannot import each other`);
            }
        }
    }
    return violations;
}

const sourceRoots = process.argv.slice(2).map((path) => resolve(path));
if (!sourceRoots.length) sourceRoots.push(resolve("src"));

const missingRoots = sourceRoots.filter((sourceRoot) => !existsSync(sourceRoot));
if (missingRoots.length) {
    console.error(`Source roots not found: ${missingRoots.join(", ")}`);
    process.exitCode = 1;
} else {
    const violations = (await Promise.all(sourceRoots.map(checkSourceRoot))).flat();
    if (violations.length) {
      console.error(violations.join("\n"));
      process.exitCode = 1;
    }
}
