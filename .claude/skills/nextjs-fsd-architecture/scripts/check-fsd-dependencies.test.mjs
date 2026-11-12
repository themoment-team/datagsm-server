import assert from "node:assert/strict";
import { mkdtemp, mkdir, rm, writeFile } from "node:fs/promises";
import { tmpdir } from "node:os";
import { join } from "node:path";
import { spawnSync } from "node:child_process";
import test from "node:test";
import { fileURLToPath } from "node:url";

const checker = fileURLToPath(new URL("./check-fsd-dependencies.mjs", import.meta.url));

async function check(importPath) {
    const root = await mkdtemp(join(tmpdir(), "fsd-check-"));
    const sourceRoot = join(root, "src");

    try {
        await mkdir(join(sourceRoot, "entities", "artist", "model"), { recursive: true });
        await mkdir(join(sourceRoot, "entities", "song", "@x"), { recursive: true });
        await writeFile(join(sourceRoot, "entities", "song", "@x", "artist.ts"), "export type Song = { id: string };\n");
        await writeFile(join(sourceRoot, "entities", "artist", "model", "artist.ts"), `import type { Song } from "${importPath}";\nexport type Artist = { songs: Song[] };\n`);

        return spawnSync(process.execPath, [checker, sourceRoot], { encoding: "utf8" });
    } finally {
        await rm(root, { recursive: true, force: true });
    }
}

test("allows entity @x public APIs for the consuming entity", async () => {
    const result = await check("@/entities/song/@x/artist");
    assert.equal(result.status, 0, result.stderr);
});

test("rejects direct imports between entity slices", async () => {
    const result = await check("@/entities/song");
    assert.equal(result.status, 1);
    assert.match(result.stderr, /entities slices cannot import each other/);
});
