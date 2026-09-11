from datetime import date, datetime

from pydantic import BaseModel, ConfigDict, Field

from app.models.task import TaskStatus


def to_camel(field_name: str) -> str:
    first, *rest = field_name.split("_")
    return first + "".join(word.capitalize() for word in rest)


class ApiModel(BaseModel):
    model_config = ConfigDict(
        alias_generator=to_camel,
        populate_by_name=True,
        from_attributes=True,
    )


class TaskCreate(ApiModel):
    title: str = Field(min_length=1, max_length=200)
    assignee: str = Field(min_length=1, max_length=100)
    due_date: date


class TaskUpdate(ApiModel):
    title: str | None = Field(default=None, min_length=1, max_length=200)
    assignee: str | None = Field(default=None, min_length=1, max_length=100)
    due_date: date | None = None
    status: TaskStatus | None = None


class StatusUpdate(ApiModel):
    status: TaskStatus


class TaskResponse(ApiModel):
    id: int
    title: str
    assignee: str
    due_date: date
    status: TaskStatus
    created_at: datetime

