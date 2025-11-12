from typing import Literal, Optional, TypeVar, Generic
from pydantic import BaseModel, ConfigDict, EmailStr


T = TypeVar('T')

class ApiResponse(BaseModel, Generic[T]):
    success: bool = True
    data: Optional[T] = None
    error: Optional[str] = None
    message: Optional[str] = None
    meta: Optional[dict] = None


class UserPreferencesSchema(BaseModel):
    email: bool
    push: bool

class UserSchema(BaseModel):
    name: str
    email: EmailStr
    push_token: Optional[str] = None
    preferences: UserPreferencesSchema


class CreateUserSchema(UserSchema):
    password: str

class TokenResponse(BaseModel):
    token: str
    token_type: Literal["bearer"] = "bearer"

class UserResponse(UserSchema):
    id: str

class LoginSchema(BaseModel):
    email: EmailStr
    password: str


class UserDataResponse(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: str
    name: str
    email: EmailStr
    push_token: str | None
    email_notification: bool
    push_notification: bool

class LoginResponseSchema(BaseModel):
    user_id: str
    token: str

class UserContactInfo(BaseModel):
    email: str
    push_token: Optional[str] = None