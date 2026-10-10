CREATE DATABASE CLIENTES
USE CLIENTES

CREATE TABLE Clientes(
ID int identity(1,1) PRIMARY KEY,
Nombre VARCHAR(60),
Email VARCHAR(40) UNIQUE,
Direccion VARCHAR(60),
Status BIT
)

INSERT INTO Clientes (Nombre, Email, Direccion, Status)
VALUES 
('Carlos Mendoza', 'cmendoza@email.com', 'Blvd. Adolfo Lopez Mateos 1204', 1),
('Laura Gonzalez', 'lgonzalez@email.com', 'Calle Francisco I. Madero 320', 1),
('Roberto Sanchez', 'rsanchez@email.com', 'Paseo del Moral 405', 0),
('Ana Torres', 'atorres@email.com', 'Av. Universidad 102', 1),
('Miguel Ramirez', 'mramirez@email.com', 'Blvd. Campestre 901', 1),
('Sofia Castro', 'scastro@email.com', 'Calle 5 de Mayo 115', 1);