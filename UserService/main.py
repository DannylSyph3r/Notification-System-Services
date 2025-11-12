import os
from typing import Annotated
from fastapi import FastAPI, status, Depends
from fastapi.responses import JSONResponse
from sqlalchemy.orm import Session
import uvicorn
from src.schemas import CreateUserSchema, LoginSchema, ApiResponse, LoginResponseSchema, UserPreferencesSchema, UserContactInfo
from src.database import get_db, Base, engine
from src.service import user_service

app = FastAPI(title="UserService", description="Service that handles usee data", version="1.0.0")

Base.metadata.create_all(engine)

@app.post("/", response_model=ApiResponse[LoginResponseSchema])
async def create_user(user_data: CreateUserSchema, db: Annotated[Session, Depends(get_db)]):
    response_data = await user_service.create_user(user_data=user_data, db=db)
    
    api_response = ApiResponse[LoginResponseSchema](
        success=True,
        data=response_data,
        message="User registered successfully"
    )
    return JSONResponse(status_code=status.HTTP_201_CREATED, content=api_response.model_dump())


@app.post("/login", response_model=ApiResponse[LoginResponseSchema])
async def login(user_data: LoginSchema, db: Annotated[Session, Depends(get_db)]):
    response_data = await user_service.handle_login(user_data=user_data, db=db)

    api_response = ApiResponse[LoginResponseSchema](
        success=True,
        data=response_data,
        message="Login successful"
    )
    return JSONResponse(status_code=status.HTTP_200_OK, content=api_response.model_dump())


@app.get("/health")
async def health_check():

    response = {
            "message": "perfectly fine"
            }

    return JSONResponse(status_code=status.HTTP_200_OK, content=response)


@app.get("/internal/users/{user_id}")
async def get_user_data(user_id: str, db: Annotated[Session, Depends(get_db)]):
    response_data = await user_service.get_user_data(id=user_id, db=db)

    api_response = ApiResponse(
        success=True,
        data=response_data,
        message="User data retrieved successfully"
    )
    return JSONResponse(status_code=status.HTTP_200_OK, content=api_response.model_dump())


@app.get("/internal/users/{user_id}/preferences", response_model=ApiResponse[UserPreferencesSchema])
async def get_user_preferences(user_id: str, db: Annotated[Session, Depends(get_db)]):
    response_data = await user_service.get_user_preferences(id=user_id, db=db)
    
    api_response = ApiResponse[UserPreferencesSchema](
        success=True,
        data=response_data,
        message="User preferences retrieved successfully"
    )
    return JSONResponse(status_code=status.HTTP_200_OK, content=api_response.model_dump())


@app.get("/internal/users/{user_id}/contact", response_model=ApiResponse[UserContactInfo])
async def get_user_contact(user_id: str, db: Annotated[Session, Depends(get_db)]):
    response_data = await user_service.get_user_contact_info(id=user_id, db=db)

    api_response = ApiResponse[UserContactInfo](
        success=True,
        data=response_data,
        message="User contact info retrieved successfully"
    )
    return JSONResponse(status_code=status.HTTP_200_OK, content=api_response.model_dump())


if __name__ == "__main__":
    port = int(os.getenv("PORT", 5000))
    uvicorn.run("main:app", host="127.0.0.1", port=port,reload=True)