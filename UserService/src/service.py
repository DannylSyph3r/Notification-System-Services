from fastapi import status, HTTPException
from sqlalchemy import select
from sqlalchemy.orm import Session
from pwdlib import PasswordHash
from src.schemas import CreateUserSchema, UserDataResponse, UserPreferencesSchema, UserResponse, LoginSchema, LoginResponseSchema, UserContactInfo
from src.models import User
from src.utils import generate_token

class UserService:

    password_hasher = PasswordHash.recommended()

    def hash_password(self, password: str) -> str:
        return self.password_hasher.hash(password)


    def verify_password(self, password, hashed_password):
        return self.password_hasher.verify(password, hashed_password)


    async def create_user(self, user_data: CreateUserSchema, db: Session) -> LoginResponseSchema:
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

        payload = {
                "user_id": str(new_user.id),
                "email": new_user.email,
                }
        token_response = await generate_token(payload, expire_delta=1440)

        response = LoginResponseSchema(
                user_id=str(new_user.id),
                token=token_response.token
                )

        return response


    async def handle_login(self, user_data:LoginSchema, db: Session) -> LoginResponseSchema:
        user = db.scalars(select(User).where(User.email == user_data.email)).first()

        if not user or not self.verify_password(user_data.password, user.password):
            raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Invalid email or password")

        payload = {
                "user_id": str(user.id), 
                "email": user.email,
                }

        token_response = await generate_token(payload=payload, expire_delta=1440)

        response = LoginResponseSchema(
                user_id=str(user.id),
                token=token_response.token
                )

        return response

    
    async def get_user_data(self, id: str, db: Session):
        user = db.scalars(select(User).where(User.id == id)).first()

        if not user:
            raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="User not found")

        response = UserDataResponse.model_validate(user)

        return response


    async def get_user_preferences(self, id: str, db: Session) -> UserPreferencesSchema:
        user = db.scalars(select(User).where(User.id == id)).first()

        if not user:
            raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="User not found")

        return UserPreferencesSchema(
            email=user.email_notification,
            push=user.push_notification
        )



    async def get_user_contact_info(self, id: str, db: Session) -> UserContactInfo:
        user = db.scalars(select(User).where(User.id == id)).first()

        if not user:
            raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="User not found")

        return UserContactInfo(
            email=user.email,
            push_token=user.push_token
        )


user_service = UserService()