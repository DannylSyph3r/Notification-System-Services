# python import 
import os
from typing import Any

# library import
import pika
from dotenv import load_dotenv

# module import

load_dotenv()


CLOUDAMQP_URL = os.getenv("AMQP_URL", "your server url")

params = pika.URLParameters(CLOUDAMQP_URL)
connection = pika.BlockingConnection(params)

channel = connection.channel()


def channel_message(queue: str, body: Any):
    channel.queue_declare(queue=queue)

    channel.basic_publish(exchange="", routing_key=queue, body=body)

    print("message sent ...")
    connection.close()
