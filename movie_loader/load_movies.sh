#!/bin/bash

FILE="movie_list.csv"

tail -n +2 "$FILE" | while IFS=',' read -r title director lead genre release_date
do
  json=$(cat <<EOF
{
  "title": "$title",
  "director": "$director",
  "lead": "$lead",
  "genre": "$genre",
  "releaseDate": "$release_date"
}
EOF
)
  curl -X POST http://localhost:8080/TicketBooker-0.1/movies \
       -H "Content-Type: application/json" \
       -d "$json"

done
echo "Movie data loaded successfully."