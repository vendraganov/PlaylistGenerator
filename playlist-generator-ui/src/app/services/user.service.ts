import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';

import { User } from '../models/user';
import { Observable, BehaviorSubject } from 'rxjs';
import { AuthenticationService } from './authentication.service';

@Injectable({ providedIn: 'root' })
export class UserService {
  
    private readonly HOST = 'http://playlist-generator.us-east-1.elasticbeanstalk.com';
    private readonly USER_URL = this.HOST + '/api/user';
    private readonly ADMIN_URL = this.HOST + '/api/admin';

    private readonly EDIT_USER = this.USER_URL + '/edit';
    private readonly UPLOAD_AVATAR = this.USER_URL + '/upload/avatar';

    private readonly GET_USERS = this.ADMIN_URL + '/users';
    private readonly DELETE_USER = this.ADMIN_URL + '/delete/user';
    private readonly EDIT_USER_BY_ADMIN = this.ADMIN_URL + '/edit/user';
    private readonly CREATE_USER_BY_ADMIN = this.ADMIN_URL + '/create/user';
    
    httpOptions = {
        headers: new HttpHeaders({'Content-Type': 'application/json'})
    };

    onUsersComponentSubject: BehaviorSubject<boolean>;
    onUsersComponent: Observable<boolean>;

    constructor(private httpClient: HttpClient, private authenticationService: AuthenticationService){
      this.onUsersComponentSubject = new BehaviorSubject<any>(false);
      this.onUsersComponent = this.onUsersComponentSubject.asObservable();

    }

    public get isOnUsersComponent(): boolean {
      return this.onUsersComponentSubject.value;
    }
  
    setOnUsersComponent(onComponent: boolean){
      this.onUsersComponentSubject.next(onComponent);
    }
    
    getUsers(): Observable<User[]> {
      return this.httpClient.get<User[]>(this.GET_USERS, {headers: this.authenticationService.getHeader()});
    }

    uploadAvatar(username: string, avatar: File): Observable<{}>{
        const formdata: FormData = new FormData();
        formdata.append('file', avatar);
        formdata.append('username', username);
     
        return this.httpClient.post(this.UPLOAD_AVATAR, formdata);
    }

    editUser(user: User): Observable<{}>{
        const editUser = 
        {   username: user.username, 
            firstName: user.firstName,
            lastName: user.lastName,
            email: user.email,
            oldUsername: user.username
        };
      return this.httpClient.put<User>(this.EDIT_USER, editUser, {headers: this.authenticationService.getHeader()});
    }

    deleteUser(username: string): Observable<{}> {
        const url = `${this.DELETE_USER}/${username}`;

        return this.httpClient.delete(url, {headers: this.authenticationService.getHeader()});
    }
  
    editUserByAdmin(user: User, oldUsername: string): Observable<{}>{
      const editUser = 
     {   username: user.username,
         firstName: user.firstName,
         lastName: user.lastName,
         email: user.email,
         role: user.role,
         oldUsername: oldUsername
     };
   return this.httpClient.put<User>(this.EDIT_USER_BY_ADMIN, editUser, {headers: this.authenticationService.getHeader()});
  }

  createUserByAdmin(username: string, password: string, firstName: string, lastName: string, email: string, userRole: string,): Observable<boolean>{
    const createUser = 
    { username: username, password: password, firstName: firstName, lastName: lastName, email: email, role: userRole };
  return this.httpClient.post<boolean>(this.CREATE_USER_BY_ADMIN, createUser, {headers: this.authenticationService.getHeader()}); 
  }

  getCloneUser(userIn: User): User{
    var userClone = new User();
    userClone.firstName = userIn.firstName;
    userClone.lastName = userIn.lastName;
    userClone.username = userIn.username;
    userClone.role = userIn.role;
    userClone.email= userIn.email;
    userClone.avatar = userIn.avatar;
    userClone.token = userIn.token;
    
    return userClone;
 }

}