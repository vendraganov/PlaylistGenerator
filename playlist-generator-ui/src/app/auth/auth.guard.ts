import { Injectable } from '@angular/core';
import { Router, CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';

import { AuthenticationService } from '../services/authentication.service';

@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate {

    constructor(private router: Router,private authenticationService: AuthenticationService) { }

    canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
        //calls method currentUserValue which returns an User if we hava one stored
        const user = this.authenticationService.currentUserValue;
        if (user) {
            // logged in so return true
            return true;
        }
        this.router.navigate(['/login']);
        return false;
    }
}