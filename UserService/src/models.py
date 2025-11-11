# python import

# library import 
from typing import Optional
from sqlalchemy import Boolean, String
from sqlalchemy.orm import Mapped, MapperEvents, mapped_column
import uuid
from uuid import UUID
from enum import Enum

from sqlalchemy.util import unique_list

# module import 
from src.database import Base


class User(Base):
    __tablename__ = "users"

    id: Mapped[UUID] = mapped_column(primary_key=True, default=uuid.uuid4(), unique=True)
    name: Mapped[str] = mapped_column(String(50), nullable=False)
    email: Mapped[str] = mapped_column(String(225), unique=True, nullable=False)
    password: Mapped[str] = mapped_column(String(225), nullable=False)
    push_token: Mapped[Optional[str]] = mapped_column(String(225), unique=True)
    email_notification: Mapped[bool] = mapped_column(Boolean, default=False)
    push_notification: Mapped[bool] = mapped_column(Boolean, default=False)


    def __repr__(self) -> str:
        email_status = "email notifications" if self.email_notification else "no email notifications"
        push_status = "push notifications" if self.push_notification else "no push notifications"
        return f"{self.name} allows {email_status}, {push_status}"
