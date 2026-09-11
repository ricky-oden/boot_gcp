from fastapi import APIRouter, Depends, Query
from sqlalchemy.orm import Session

from app.database import get_db
from app.models.task import TaskStatus
from app.repositories.task_repository import TaskRepository
from app.schemas.task import StatusUpdate, TaskCreate, TaskResponse, TaskUpdate
from app.services.task_service import TaskService


router = APIRouter(prefix="/tasks", tags=["tasks"])


def get_service(db: Session = Depends(get_db)) -> TaskService:
    # Dependsは教材上のDI例です。実案件での使用を示すものではありません。
    return TaskService(TaskRepository(db))


@router.get("", response_model=list[TaskResponse])
def list_tasks(
    status: TaskStatus | None = Query(default=None),
    assignee: str | None = Query(default=None),
    service: TaskService = Depends(get_service),
):
    return service.list_tasks(status=status, assignee=assignee)


@router.post("", response_model=TaskResponse, status_code=201)
def create_task(request: TaskCreate, service: TaskService = Depends(get_service)):
    return service.create_task(request)


@router.put("/{task_id}", response_model=TaskResponse)
def update_task(
    task_id: int,
    request: TaskUpdate,
    service: TaskService = Depends(get_service),
):
    return service.update_task(task_id, request)


@router.patch("/{task_id}/status", response_model=TaskResponse)
def change_status(
    task_id: int,
    request: StatusUpdate,
    service: TaskService = Depends(get_service),
):
    return service.change_status(task_id, request.status)

