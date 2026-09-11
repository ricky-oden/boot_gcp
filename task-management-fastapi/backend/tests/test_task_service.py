from datetime import date
from unittest.mock import Mock

import pytest

from app.models.task import Task, TaskStatus
from app.repositories.task_repository import TaskRepository
from app.schemas.task import TaskUpdate
from app.services.task_service import (
    InvalidStatusTransitionError,
    TaskNotFoundError,
    TaskService,
)


def test_update_task_changes_assignee_due_date_and_status():
    # Arrange
    repository = Mock(spec=TaskRepository)
    task = Task(
        title="見積確認",
        assignee="佐藤",
        due_date=date(2026, 9, 10),
        status=TaskStatus.TODO,
    )
    repository.find_by_id.return_value = task
    repository.save.side_effect = lambda saved: saved
    service = TaskService(repository)
    request = TaskUpdate(
        assignee="鈴木", dueDate=date(2026, 9, 15), status=TaskStatus.IN_PROGRESS
    )

    # Act
    result = service.update_task(1, request)

    # Assert
    assert result.assignee == "鈴木"
    assert result.due_date == date(2026, 9, 15)
    assert result.status == TaskStatus.IN_PROGRESS
    repository.save.assert_called_once_with(task)


def test_update_missing_task_raises_error():
    # Arrange
    repository = Mock(spec=TaskRepository)
    repository.find_by_id.return_value = None
    service = TaskService(repository)

    # Act / Assert
    with pytest.raises(TaskNotFoundError, match="Task not found: id=99"):
        service.update_task(99, TaskUpdate(assignee="鈴木"))
    repository.save.assert_not_called()


def test_invalid_status_transition_raises_error():
    # Arrange
    repository = Mock(spec=TaskRepository)
    task = Task(
        title="見積確認",
        assignee="佐藤",
        due_date=date(2026, 9, 10),
        status=TaskStatus.TODO,
    )
    repository.find_by_id.return_value = task
    service = TaskService(repository)

    # Act / Assert
    with pytest.raises(InvalidStatusTransitionError, match="TODO -> DONE"):
        service.change_status(1, TaskStatus.DONE)
    repository.save.assert_not_called()

