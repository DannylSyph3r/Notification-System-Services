# python import

# library import 
from sqlalchemy import create_engine
from sqlalchemy.orm import DeclarativeBase, sessionmaker
# module import

engine = create_engine("sqlite:///user.db")
sessionLocal = sessionmaker(bind=engine, autoflush=False, autocommit=False)

class Base(DeclarativeBase):
    pass

async def get_db():
    db = sessionLocal()
    try:
        yield db
    finally:
        db.close()
