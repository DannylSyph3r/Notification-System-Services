# python import
import os

# library import 
import pika
from dotenv import load_dotenv

# module import

load_dotenv()

CLOUDAMQP_URL = os.getenv("AMQP_URL", "your server url")

params = pika.URLParameters(CLOUDAMQP_URL)
connection = pika.BlockingConnection(params)

channel = connection.channel()

channel.queue_declare(queue="user_registration")

def callback(ch, method, properties, body):
    print(f" Reveived queue data: {body.decode()}")

channel.basic_consume(queue="user_registration", on_message_callback=callback, auto_ack=True)

print("waiting for messages")
channel.start_consuming()
