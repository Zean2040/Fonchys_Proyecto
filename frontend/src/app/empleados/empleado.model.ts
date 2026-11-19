export interface Empleado {
  id?: number;
  nombre: string;
  apellido: string;
  cargo: string;
  telefono?: string | null;
  email?: string | null;
  activo?: boolean;
}
