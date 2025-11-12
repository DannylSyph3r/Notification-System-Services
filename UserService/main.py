# python import 
import os
from typing import Annotated

# library import 
from fastapi import FastAPI, status, Depends
from fastapi.responses import JSONResponse
from sqlalchemy.orm import Session
import uvicorn

# module import 
from src.schemas import CreateUserSchema, LoginSchema
from src.database import get_db, Base, engine
from src.service import user_service

app = FastAPI(title="UserService", description="Service that handles usee data", version="1.0.0")

Base.metadata.create_all(engine)

@app.post("/")
async def create_user(user_data: CreateUserSchema, db: Annotated[Session, Depends(get_db)]):
    response = await user_service.create_user(user_data=user_data, db=db)

    return JSONResponse(status_code=status.HTTP_201_CREATED, content=response.model_dump())


@app.post("/login")
async def login(user_data: LoginSchema, db: Annotated[Session, Depends(get_db)]):
    response = await user_service.handle_login(user_data=user_data, db=db)

    return JSONResponse(status_code=status.HTTP_200_OK, content=response.model_dump())


@app.get("/health")
async def health_check():

    response = {
            "message": "perfectly fine"
            }

    return JSONResponse(status_code=status.HTTP_200_OK, content=response)


@app.get("/{user_id}")
async def get_user_data(user_id: str, db: Annotated[Session, Depends(get_db)]):
    response = await user_service.get_user_data(id=user_id, db=db)

    return JSONResponse(status_code=status.HTTP_200_OK, content=response.model_dump())


if __name__ == "__main__":
    port = int(os.getenv("PORT", 5000))
    uvicorn.run("main:app", host="127.0.0.1", port=port,reload=True)
