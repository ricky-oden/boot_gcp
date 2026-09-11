from app.models.task import Task, TaskStatus
from app.repositories.task_repository import TaskRepository
from app.schemas.task import TaskCreate, TaskUpdate


class TaskNotFoundError(Exception):
    pass


class InvalidStatusTransitionError(Exception):
    pass


class TaskService:
    def __init__(self, repository: TaskRepository):
        self.repository = repository

    def list_tasks(
        self, status: TaskStatus | None = None, assignee: str | None = None
    ) -> list[Task]:
        return self.repository.find_all(status=status, assignee=assignee)

    def create_task(self, request: TaskCreate) -> Task:
        task = Task(
            title=request.title.strip(),
            assignee=request.assignee.strip(),
            due_date=request.due_date,
            status=TaskStatus.TODO,
        )
        return self.repository.save(task)

    def update_task(self, task_id: int, request: TaskUpdate) -> Task:
        task = self._get(task_id)
        values = request.model_dump(exclude_none=True)

        if "status" in values:
            self._validate_transition(task.status, values["status"])
        for field_name, value in values.items():
            setattr(task, field_name, value.strip() if isinstance(value, str) else value)
        return self.repository.save(task)

    def change_status(self, task_id: int, new_status: TaskStatus) -> Task:
        task = self._get(task_id)
        self._validate_transition(task.status, new_status)
        task.status = new_status
        return self.repository.save(task)

    def _get(self, task_id: int) -> Task:
        task = self.repository.find_by_id(task_id)
        if task is None:
            raise TaskNotFoundError(f"Task not found: id={task_id}")
        return task

    @staticmethod
    def _validate_transition(current: TaskStatus, new: TaskStatus) -> None:
        allowed = {
            TaskStatus.TODO: TaskStatus.IN_PROGRESS,
            TaskStatus.IN_PROGRESS: TaskStatus.DONE,
        }
        if allowed.get(current) != new:
            raise InvalidStatusTransitionError(
                f"Invalid status transition: {current.value} -> {new.value}"
            )

