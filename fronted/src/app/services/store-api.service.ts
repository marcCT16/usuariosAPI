import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { CrearFacturaInput, Factura, Producto, ProductoInput, Usuario } from '../models/store.models';

@Injectable({ providedIn: 'root' })
export class StoreApiService {
  private readonly http = inject(HttpClient);

  getProducts(): Observable<Producto[]> {
    return this.http.get<Producto[]>('/productos');
  }

  createProduct(product: ProductoInput): Observable<Producto> {
    return this.http.post<Producto>('/productos', product);
  }

  addProductStock(productId: number, unidades: number): Observable<Producto> {
    return this.http.put<Producto>(`/productos/${productId}/stock`, { unidades });
  }

  getUsers(): Observable<Usuario[]> {
    return this.http.get<Usuario[]>('/usuarios');
  }

  createUser(user: Usuario): Observable<Usuario> {
    return this.http.post<Usuario>('/usuarios', user);
  }

  getOrders(userKey: string): Observable<Factura[]> {
    return this.http.get<Factura[]>('/facturas', { params: { usuarioClave: userKey } });
  }

  createOrder(order: CrearFacturaInput): Observable<Factura> {
    return this.http.post<Factura>('/facturas', order);
  }
}