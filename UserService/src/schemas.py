# python import
from typing import Literal, Optional

# library import 
from pydantic import BaseModel, EmailStr

# module import


class UserPreferenceSchema(BaseModel):
    email: bool
    push: bool

class UserSchema(BaseModel):
    name: str
    email: EmailStr
    push_token: Optional[str] = None
    preferences: UserPreferenceSchema


class CreateUserSchema(UserSchema):
    password: str

class TokenResponse(BaseModel):
    token: str
    token_type: Literal["bearer"] = "bearer"

class UserResponse(UserSchema):
    id: str
    access_token: TokenResponse
