import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = environment.apiUrl + '/rest/users';

  constructor(private http: HttpClient) { }

  register(username: string, password: string, gender: string) {
    return this.http.get<{success: boolean}>(`${this.apiUrl}/register`, {
      params: { username, password, gender }
    });
  }

  login(username: string, password: string) {
    return this.http.get<{success: boolean}>(`${this.apiUrl}/login`, {
      params: { username, password }
    });
  }
}