from contextlib import asynccontextmanager

from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse

from app.database import Base, engine
from app.routers.tasks import router as tasks_router
from app.services.task_service import InvalidStatusTransitionError, TaskNotFoundError


@asynccontextmanager
async def lifespan(_app: FastAPI):
    Base.metadata.create_all(bind=engine)
    yield


app = FastAPI(
    title="法人向け業務依頼・進捗管理Webシステム 学習用ミニ版",
    lifespan=lifespan,
)
app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:3001"],
    allow_methods=["GET", "POST", "PUT", "PATCH"],
    allow_headers=["*"],
)
app.include_router(tasks_router)

@app.exception_handler(TaskNotFoundError)
def handle_not_found(_request: Request, exception: TaskNotFoundError):
    return JSONResponse(status_code=404, content={"message": str(exception)})


@app.exception_handler(InvalidStatusTransitionError)
def handle_invalid_transition(
    _request: Request, exception: InvalidStatusTransitionError
):
    return JSONResponse(status_code=409, content={"message": str(exception)})
