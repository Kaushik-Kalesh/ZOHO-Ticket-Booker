import requests
import random
import string
import time

BASE_URL = "http://localhost:8080/TicketBooker-0.1"

NUM_BOTS = 100
MIN_VOTES = 5
MAX_VOTES = 15


def random_string(length=8):
    return ''.join(random.choices(string.ascii_lowercase + string.digits, k=length))

def get_movie_ids():
    resp = requests.get(f"{BASE_URL}/movies?type=ids")
    resp.raise_for_status()
    data = resp.json()
    return data["movie_ids"]

def register_bot():
    username = "bot_" + random_string(6)
    email = username + "@botmail.com"
    password = "password123"

    payload = {
        "username": username,
        "email": email,
        "password": password
    }

    resp = requests.post(f"{BASE_URL}/login?type=bot", json=payload)
    resp.raise_for_status()

    data = resp.json()

    if "status" not in data or data["status"] != "success":
        raise Exception("Registration failed: " + str(data))

    return data["user_id"]

def vote(user_id, movie_id):
    payload = {
        "user_id": user_id,
        "movie_id": movie_id
    }

    resp = requests.post(f"{BASE_URL}/vote_movie", json=payload)
    resp.raise_for_status()


def main():
    print("Fetching movie IDs...")
    movie_ids = get_movie_ids()

    print(f"Found {len(movie_ids)} movies.")

    for i in range(NUM_BOTS):
        try:
            user_id = register_bot()

            num_votes = random.randint(MIN_VOTES, MAX_VOTES)
            chosen_movies = random.sample(movie_ids, min(num_votes, len(movie_ids)))

            for movie_id in chosen_movies:
                vote(user_id, movie_id)

            time.sleep(0.1)

        except Exception as e:
            print(f"Error creating bot {i+1}: {e}")

    print(f"Created {NUM_BOTS} bots and cast votes for movies.")

if __name__ == "__main__":
    main()
