---
paths:
  - "**/domain/student/service/impl/*ServiceImpl.kt"
  - "**/domain/club/service/impl/*ServiceImpl.kt"
  - "**/domain/project/service/impl/*ServiceImpl.kt"
---

# Domain Event Rules

## Publishing Rule

A service in the `student`/`club`/`project` domains holding a non-readOnly `@Transactional` method
must publish `EventDispatchRequested` through `ApplicationEventPublisher`. This applies to every
module, not just `datagsm-web`.

```kotlin
// CORRECT
@Service
class DeleteProjectServiceImpl(
    private val projectJpaRepository: ProjectJpaRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
) : DeleteProjectService {
    @Transactional
    override fun execute(projectId: Long) {
        val project = ...
        val oldObj = generateProjectEventObject(project)
        projectJpaRepository.delete(project)

        applicationEventPublisher.publishEvent(
            EventDispatchRequested(
                EventType.PROJECT_UPDATED,
                EventChangedData(
                    old = listOf(EventChangeItem(0, oldObj)),
                    new = listOf(EventChangeItem(0, EmptyEventObject())),
                ),
            ),
        )
    }
}
```

- Never call `EventPublisher.dispatch` from a service — `EventDispatchListener` in `datagsm-common`
  consumes the event after commit and dispatches it
- Create: `old = EmptyEventObject()`, `new = 생성된 객체`
- Delete: `old = 삭제 전 객체`, `new = EmptyEventObject()` — snapshot `old` **before** the delete runs
- Bulk JPQL bypasses the persistence context, so snapshot before executing it

## Build-time Check

`EventDispatchConventionTest` in `datagsm-common` scans every module's sources and fails when a
write-transactional service never references `EventDispatchRequested`.

## Allowlist

A service that intentionally skips publishing is registered in its own module's
`event-dispatch-allowlist.txt` as `<ClassName> # <사유>`.

```
# datagsm-web/event-dispatch-allowlist.txt
SomeServiceImpl # 상위 서비스에서 일괄 발행하므로 중복 발행 방지
```

- An entry without a reason fails the test — a forgotten publish must stay distinguishable from a
  deliberate one
- An entry that no longer applies (the class publishes now, or was removed) also fails the test
