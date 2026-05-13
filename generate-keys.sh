#!/bin/bash
# Generate RSA key pair for JWT signing (RS256)
set -e

KEYS_DIR="./keys"
mkdir -p "$KEYS_DIR"

if [ -f "$KEYS_DIR/private.pem" ]; then
    echo "Keys already exist in $KEYS_DIR. Skipping generation."
    echo "To regenerate, delete the keys directory first."
    exit 0
fi

echo "Generating RSA 2048-bit key pair..."
openssl genrsa -out "$KEYS_DIR/private.pem" 2048
openssl rsa -in "$KEYS_DIR/private.pem" -pubout -out "$KEYS_DIR/public.pem"

echo "Keys generated successfully:"
echo "  Private key: $KEYS_DIR/private.pem"
echo "  Public key:  $KEYS_DIR/public.pem"
