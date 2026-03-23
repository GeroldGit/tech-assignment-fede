#!/usr/bin/env bash

set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
ADMIN_USER="${ADMIN_USER:-admin}"
ADMIN_PASS="${ADMIN_PASS:-admin123}"
USER_USER="${USER_USER:-user}"
USER_PASS="${USER_PASS:-user123}"

RESPONSE_STATUS=""
RESPONSE_BODY=""

print_step() {
  local message="$1"
  printf "\n==> %s\n" "$message"
}

request() {
  local method="$1"
  local url="$2"
  local user="$3"
  local pass="$4"
  local data="${5:-}"

  local tmp
  tmp="$(mktemp)"

  if [[ -n "$data" ]]; then
    RESPONSE_STATUS="$(curl -sS -u "$user:$pass" -X "$method" "$url" \
      -H "Content-Type: application/json" \
      -d "$data" \
      -o "$tmp" -w "%{http_code}")"
  else
    RESPONSE_STATUS="$(curl -sS -u "$user:$pass" -X "$method" "$url" \
      -o "$tmp" -w "%{http_code}")"
  fi

  RESPONSE_BODY="$(cat "$tmp")"
  rm -f "$tmp"
}

assert_status() {
  local expected="$1"
  local step_name="$2"

  if [[ "$RESPONSE_STATUS" != "$expected" ]]; then
    echo "[FAIL] $step_name"
    echo "Expected HTTP: $expected"
    echo "Actual HTTP:   $RESPONSE_STATUS"
    echo "Body: $RESPONSE_BODY"
    exit 1
  fi

  echo "[OK] $step_name (HTTP $RESPONSE_STATUS)"
}

extract_id() {
  echo "$RESPONSE_BODY" | sed -n 's/.*"id"[[:space:]]*:[[:space:]]*\([0-9][0-9]*\).*/\1/p' | head -n 1
}

print_db_state() {
  local stage="$1"

  request "GET" "$BASE_URL/api/v1/pets" "$USER_USER" "$USER_PASS"
  if [[ "$RESPONSE_STATUS" != "200" ]]; then
    echo "[WARN] Could not fetch DB state after $stage (HTTP $RESPONSE_STATUS)"
    echo "Body: $RESPONSE_BODY"
    return
  fi

  echo "DB snapshot after $stage:"
  if command -v jq >/dev/null 2>&1; then
    echo "$RESPONSE_BODY" | jq .
  else
    echo "$RESPONSE_BODY"
  fi
}

print_step "Checking read access as USER"
request "GET" "$BASE_URL/api/v1/pets" "$USER_USER" "$USER_PASS"
assert_status "200" "GET /api/v1/pets as USER"

print_step "Creating pet as ADMIN"
request "POST" "$BASE_URL/api/v1/pets" "$ADMIN_USER" "$ADMIN_PASS" '{"name":"Smoke Buddy","species":"Dog","age":2,"ownerName":"Smoke Owner"}'
assert_status "201" "POST /api/v1/pets as ADMIN"
PET_ID="$(extract_id)"
if [[ -z "$PET_ID" ]]; then
  echo "[FAIL] Could not extract pet id from create response"
  echo "Body: $RESPONSE_BODY"
  exit 1
fi

echo "Created pet with id: $PET_ID"
print_db_state "CREATE"

print_step "Reading created pet as USER"
request "GET" "$BASE_URL/api/v1/pets/$PET_ID" "$USER_USER" "$USER_PASS"
assert_status "200" "GET /api/v1/pets/{id} as USER"

print_step "Updating pet as ADMIN"
request "PUT" "$BASE_URL/api/v1/pets/$PET_ID" "$ADMIN_USER" "$ADMIN_PASS" '{"name":"Smoke Buddy Updated","species":"Dog","age":3,"ownerName":"Smoke Owner"}'
assert_status "200" "PUT /api/v1/pets/{id} as ADMIN"
print_db_state "UPDATE"

print_step "Deleting pet as ADMIN"
request "DELETE" "$BASE_URL/api/v1/pets/$PET_ID" "$ADMIN_USER" "$ADMIN_PASS"
assert_status "204" "DELETE /api/v1/pets/{id} as ADMIN"
print_db_state "DELETE"

print_step "Verifying resource is gone"
request "GET" "$BASE_URL/api/v1/pets/$PET_ID" "$USER_USER" "$USER_PASS"
assert_status "404" "GET deleted pet returns 404"

echo "\nSmoke test completed successfully."
