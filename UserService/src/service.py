#python import

# library import 
from fastapi import status, HTTPException
from sqlalchemy import select
from sqlalchemy.orm import Session
from pwdlib import PasswordHash


# module import
from src.schemas import CreateUserSchema, UserPreferenceSchema, UserResponse
from src.models import User
from src.utils import generate_token

class UserService:

    password_hasher = PasswordHash.recommended()

    def hash_password(self, password: str) -> str:
        return self.password_hasher.hash(password)


    async def create_user(self, user_data: CreateUserSchema, db: Session) -> UserResponse:
        existing = db.scalars(select(User).where(User.email == user_data.email)).first()

        if existing:
            raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="User already exists")

        hashed_password = self.hash_password(user_data.password)

        new_user = User(name=user_data.name,
                        email=user_data.email,
                        push_token=user_data.push_token,
                        email_notification=user_data.preferences.email,
                        push_notification=user_data.preferences.push,
                        password=hashed_password
                    )

        db.add(new_user)
        db.commit()
        db.refresh(new_user)

        # create access token
        payload = {
                "sub": str(new_user.id),
                "email": new_user.email,
                "push_id": new_user.push_token
                }
        token = await generate_token(payload, expire_delta=30)

        response = UserResponse(
                id=str(new_user.id),
                name=new_user.name,
                email=new_user.email,
                push_token=new_user.push_token,
                preferences=UserPreferenceSchema(
                    email=new_user.email_notification,
                    push=new_user.push_notification
                    ),
                access_token=token
                )

        return response

user_service = UserService()
