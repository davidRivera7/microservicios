namespace ClientesAPI.Models
{
    public class Cliente
    {
        public int Id { get; set; }
        public string Nombre { get; set; } 
        public string Email { get; set; } 
        public string Direccion { get; set; }
        public int Status {  get; set; } = 1; //Por defecto tendra estatus alta al crearse
    }
}
