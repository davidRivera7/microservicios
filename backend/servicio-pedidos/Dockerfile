# Etapa 1: Compilación del binario de Go
FROM golang:1.22-alpine AS builder

WORKDIR /app

# Copiar archivos de dependencias
COPY go.mod go.sum* ./
RUN go mod download

# Copiar todo el código fuente
COPY . .

# Compilar un ejecutable estático de Go
RUN CGO_ENABLED=0 GOOS=linux go build -o main .

# Etapa 2: Imagen final ultra ligera (Scratch o Alpine)
FROM alpine:latest

WORKDIR /app

# Copiar solo el binario compilado de la etapa anterior
COPY --from=builder /app/main .

EXPOSE 8082

ENTRYPOINT ["./main"]