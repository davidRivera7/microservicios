using System.Collections.Generic;
using Microsoft.Data.SqlClient;
using Dapper;
using ClientesAPI.Models;   

namespace ClientesAPI.Repository
{
    public class ClienteRepository
    {
        private readonly string conexion; 
        public ClienteRepository(IConfiguration config)
        {
            conexion = config.GetConnectionString("ConexionSQL");
        }

        public IEnumerable<Cliente> ObtenerClientes(int status) {
           using var con = new SqlConnection(conexion);
            var query = "SELECT Id, Nombre, Email, Direccion, Status FROM Clientes  WHERE Status=" + status ;
            return con.Query<Cliente>(query);
        }

        public Cliente cargarClientePorID(int id) {
            using var connection = new SqlConnection(conexion);
            var sql = "SELECT Id, Nombre, Email, Direccion FROM Clientes WHERE Id = @Id ";
            return connection.QueryFirstOrDefault<Cliente>(sql, new { Id = id });
        }

        public void Guardar(Cliente cliente)
        {
            using var connection = new SqlConnection(conexion);
            var sql = "INSERT INTO Clientes (Nombre, Email, Direccion,Status) VALUES (@Nombre, @Email, @Direccion, @Status)";
            connection.Execute(sql, cliente);
        }

        public void Actualizar(int id, Cliente cliente)
        {
            using var connection = new SqlConnection(conexion);
            var sql = "UPDATE Clientes SET Nombre = @Nombre, Email = @Email, Direccion = @Direccion WHERE Id = @Id";
            connection.Execute(sql, new { cliente.Nombre, cliente.Email, cliente.Direccion, Id = id });
        }

        public void CambiarStatus(int id,int status)
        {
            using var connection = new SqlConnection(conexion);
            var sql = "UPDATE Clientes SET Status = @Status WHERE Id = @Id";
            connection.Execute(sql, new { Id = id, Status = status });
        }

        public Cliente? ObtenerPorCorreo(string correo) {
            using var connection = new SqlConnection(conexion);
            var sql = "SELECT Id, Nombre, Email, Direccion FROM Clientes WHERE Email = @Correo";
            return connection.QueryFirstOrDefault<Cliente>(sql, new { Correo = correo });
        }
    }
}