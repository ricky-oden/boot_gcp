from sqlalchemy import select
from sqlalchemy.orm import Session

from app.models.task import Task, TaskStatus


class TaskRepository:
    """教材上、DB責務を分けるために採用した構成です。実案件での採用は未確定です。"""

    def __init__(self, db: Session):
        self.db = db

    def find_all(
        self, status: TaskStatus | None = None, assignee: str | None = None
    ) -> list[Task]:
        statement = select(Task).order_by(Task.created_at.desc())
        if status is not None:
            statement = statement.where(Task.status == status)
        if assignee:
            statement = statement.where(Task.assignee.ilike(f"%{assignee}%"))
        return list(self.db.scalars(statement))

    def find_by_id(self, task_id: int) -> Task | None:
        return self.db.get(Task, task_id)

    def save(self, task: Task) -> Task:
        self.db.add(task)
        self.db.commit()
        self.db.refresh(task)
        return task

