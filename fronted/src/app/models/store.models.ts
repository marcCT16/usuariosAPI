export interface Producto {
  id: number;
  nombre: string;
  descripcion: string | null;
  precio: number;
  stock: number;
}

export interface Usuario {
  clave: string;
  nombre: string;
  codigo: string;
}

export interface ItemFacturaInput {
  productoId: number;
  cantidad: number;
}

export interface CrearFacturaInput {
  usuarioClave: string;
  productos: ItemFacturaInput[];
}

export interface DetalleFactura {
  productoId: number;
  nombreProducto: string;
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
}

export interface Factura {
  id: number;
  usuarioClave: string;
  fecha: string;
  total: number;
  productos: DetalleFactura[];
}

export interface ProductoInput {
  nombre: string;
  descripcion: string;
  precio: number;
  stock: number;
}