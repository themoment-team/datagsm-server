# GitHub Reply Formats

Use these templates when posting inline replies in Step 5.
Always quote `comment_id` to prevent shell injection.

## VALID — fix succeeded

```
Addressed in <abc1234>. (Ref: <source>)
```

## VALID — fix failed

```
The concern is valid. Manual intervention required — will handle separately.
```

## INVALID

```
This suggestion conflicts with the project's conventions.
Ref: <source> — "<rule quote>"
The current code correctly follows the rule and will not be changed.
```

## PARTIAL — accepted

```
Partially agreed and addressed in <abc1234>.
```

## PARTIAL — rejected

```
After review, decided not to apply this change.
```

## PARTIAL — pending

```
Under review. Will follow up.
```
