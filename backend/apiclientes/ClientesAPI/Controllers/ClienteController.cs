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
    /// <summary>
    /// Da de baja/elimina logicamente a un cliente por su id
    /// </summary>
    /// <param name="id"></param>
    /// <returns></returns>

    [HttpDelete("DarDeBaja/{id}")]
    public IActionResult Eliminar(int id)
    {
        var clienteExistente = repositorio.cargarClientePorID(id);
        if (clienteExistente == null) return NotFound();
        repositorio.DarDeBaja(id);

        return NoContent();
    }
    /// <summary>
    /// Autentica a los cliente para darle acceso al sistema , con base a correo y contraseña.
    /// </summary>
    /// <param name="correo"></param>
    /// <param name="pass"></param>
    /// <returns></returns>

    [HttpPost("Autenticar/{correo}/{pass}")]
    public IActionResult ObtenerPorCorreo(string correo,string pass)
    {
        var cliente = repositorio.Autenticar(correo,pass);
        if (cliente == null)
        {
            return NotFound(new { mensaje = "No existe el usuario con los datos ingresados." });
        }
        return Ok(cliente);
    }
}