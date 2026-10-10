using Microsoft.AspNetCore.Mvc;
using ClientesAPI.Models;
using ClientesAPI.Repository;

namespace ApiClientes.Controllers;

[ApiController]
[Route("api/[controller]")]
public class ClientesController : ControllerBase
{
    private readonly ClienteRepository repositorio;

    public ClientesController(ClienteRepository repository)
    {
        repositorio = repository;
    }

    [HttpGet("ObtenerTodos/{status}")]
    public IActionResult ObtenerTodos(int status)
    {
        return Ok(repositorio.ObtenerClientes(status));
    }

    /// <summary>
    /// Encargado de obtener las propiedades de un cliente especifico.
    /// Si no se encuentran datos del cliente se retorna codigo 404
    /// </summary>
    /// <param name="id"></param>
    /// <returns></returns>
    [HttpGet("ObtenerPorID/{id}")]
    public IActionResult ObtenerPorId(int id)
    {
        var cliente = repositorio.cargarClientePorID(id);
        if (cliente == null) return NotFound();
        return Ok(cliente);
    }

    /// <summary>
    /// Inserta un nuevo cliente
    /// </summary>
    /// <param name="cliente"></param>
    /// <returns></returns>
    [HttpPost("Insertar")]
    public IActionResult Registrar([FromBody] Cliente cliente)
    {
       repositorio.Guardar(cliente);
       return Ok(new { mensaje = "Cliente registrado correctamente" });
    }
    
    /// <summary>
    /// Valida previamente que el registro del cliente exista en la BD
    /// </summary>
    /// <param name="id"></param>
    /// <param name="cliente"></param>
    /// <returns></returns>
    [HttpPut("Actualizar/{id}")]
    public IActionResult Actualizar(int id, [FromBody] Cliente cliente)
    {
        var registro = repositorio.cargarClientePorID(id);
        if (registro == null) return NotFound();

        repositorio.Actualizar(id, cliente);
        return Ok(new { mensaje = "Cliente actualizado correctamente" });
    }

    [HttpDelete("CambiarStatus/{id}/{status}")]
    public IActionResult Eliminar(int id,int status)
    {
        var clienteExistente = repositorio.cargarClientePorID(id);
        if (clienteExistente == null) return NotFound();
        repositorio.CambiarStatus(id, status);

        return NoContent();
    }

    [HttpGet("ObtenerPorCorreo/{correo}")]
    public IActionResult ObtenerPorCorreo(string correo)
    {
        var cliente = repositorio.ObtenerPorCorreo(correo);
        if (cliente == null)
        {
            return NotFound(new { mensaje = "El correo no existe en la base de datos." });
        }
        return Ok(cliente);
    }
}