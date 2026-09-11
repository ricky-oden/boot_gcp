import os

from sqlalchemy import create_engine
from sqlalchemy.orm import DeclarativeBase, sessionmaker


DATABASE_URL = os.getenv(
    "DATABASE_URL",
    "postgresql+psycopg://workflow:workflow@localhost:5433/task_management",
)

engine = create_engine(DATABASE_URL)
SessionLocal = sessionmaker(bind=engine, autoflush=False, expire_on_commit=False)


class Base(DeclarativeBase):
    pass


def get_db():
    # withと同様に、finallyで必ずDB Sessionを閉じます。
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

