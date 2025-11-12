import os
from datetime import datetime, time, timedelta, timezone
import jwt
from dotenv import load_dotenv
from src.schemas import TokenResponse

load_dotenv()

SECRET_KEY = os.getenv("SECRET_KEY", "your secret key")
ALGORITHM = os.getenv("ALGORITHM", "HS256")
ACCESS_TOKEN_EXPIRE_MINUTES = int(os.getenv("ACCESS_TOKEN_EXPIRE_MINUTES", "60"))

async def generate_token(payload: dict, expire_delta: int | None = None) -> TokenResponse:

    to_encode = payload.copy()

    if expire_delta:
        expire = datetime.now(timezone.utc) + timedelta(minutes=expire_delta)
    else:
        expire = datetime.now(timezone.utc) + timedelta(minutes=15)

    to_encode.update({"exp": expire})

    encoded_token = jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)

    token_response = TokenResponse(token=encoded_token)

    return token_response

