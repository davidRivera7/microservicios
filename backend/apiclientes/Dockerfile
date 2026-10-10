# Etapa 1: Runtime de .NET 8 (Entorno donde se ejecutara la API)
FROM mcr.microsoft.com/dotnet/aspnet:8.0 AS base
USER app
WORKDIR /app
EXPOSE 8083
ENV ASPNETCORE_URLS=http://+:8083

# Etapa 2: SDK y compilacion (Entorno donde se construye el codigo)
FROM mcr.microsoft.com/dotnet/sdk:8.0 AS build
ARG BUILD_CONFIGURATION=Release
WORKDIR /src

# Copiar el archivo del proyecto y restaurar paquetes NuGet
COPY ["ClientesAPI/ClientesAPI.csproj", "ClientesAPI/"]
RUN dotnet restore "ClientesAPI/ClientesAPI.csproj"

# Copiar el resto del codigo fuente y compilar
COPY . .
WORKDIR "/src/ClientesAPI"
RUN dotnet build "ClientesAPI.csproj" -c $BUILD_CONFIGURATION -o /app/build

# Etapa 3: Publicacion
FROM build AS publish
ARG BUILD_CONFIGURATION=Release
RUN dotnet publish "ClientesAPI.csproj" -c $BUILD_CONFIGURATION -o /app/publish /p:UseAppHost=false

# Etapa 4: Imagen final
FROM base AS final
WORKDIR /app
COPY --from=publish /app/publish .
ENTRYPOINT ["dotnet", "ClientesAPI.dll"]