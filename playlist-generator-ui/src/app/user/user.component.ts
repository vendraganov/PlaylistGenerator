import { Component, OnInit } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
import { User } from '../models/user';
import { AuthenticationService } from '../services/authentication.service';
import { UserService } from '../services/user.service';
import { Router } from '@angular/router';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';

@Component({
  selector: 'user',
  templateUrl: './user.component.html',
  styleUrls: ['./user.component.scss']
})
export class UserComponent implements OnInit {

  user: User;
  oldUser: User;
  role: string;
  selectedFile: File = null;
  avatar: any;
  edditing: boolean = false;
  hasImage: boolean = false;
  editForm: FormGroup;

  constructor(private formBuilder: FormBuilder, private authenticationService: AuthenticationService, private userService: UserService,
    private router: Router, private domSanitizer: DomSanitizer) { 

  }

  ngOnInit() {
    
    this.authenticationService.currentUser.subscribe(currentUser => 
      { this.user = currentUser
       this.oldUser = this.userService.getCloneUser(currentUser)
       this.displayRole(this.user.role);
      });
    if(this.user.avatar !== undefined && this.user.avatar !== null){
      this.hasImage = true;
    }

    this.editForm = this.formBuilder.group({
      firstName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(15)]],
      lastName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(15)]],
      email: ['', [Validators.required, Validators.email]]
     });
  }

  get field() { return this.editForm.controls; }

  onSubmit(event) {
      this.edditing = !this.edditing;
      if (this.editForm.invalid) {
          return;
      }

      if(!this.edditing){

        this.user.firstName = event.value.firstName;
        this.user.lastName = event.value.lastName;
        this.user.email = event.value.email;

        this.userService.editUser(this.user).subscribe(data => {
            console.log(data);
        },error => {
          this.authenticationService.saveEditUser(this.oldUser);
          alert("Error: "+ error);
          },
          () => {
            this.authenticationService.saveEditUser(this.user);
          });
        
        }
  }

onUploadAvatar(event){
    this.selectedFile = event.target.files[0];
    if(this.checkImageType(this.selectedFile)){
      this.userService.uploadAvatar(this.user.username, this.selectedFile).subscribe(data => {
        console.log(data);
    },error => {
      alert("Error: "+ error);
      },() => { 
        this.saveAvatar(this.selectedFile);
      });
    }
    else{
      alert("Image must be a png format!");
    }
   
}

checkImageType(file: File): boolean{
 return file.type.split("/")[file.type.split("/").length-1] === "png";
}

saveAvatar(avatarFile: File){
  var reader:FileReader = new FileReader();
  reader.onloadend = (e) => {
    this.avatar = reader.result;
    this.user.avatar = this.avatar.replace("data:image/png;base64,","");
    this.authenticationService.saveEditUser(this.user);
    this.ngOnInit();
  }
  reader.readAsDataURL(avatarFile);
}

cansel(){
  if(this.edditing){
    this.edditing = !this.edditing;
  }else{
    this.router.navigate(['/playlists-dashboard']);
  }
 
}

displayRole(roleIn: string){
   switch(roleIn){
      case "ROLE_USER":{
        this.role = "User";
        break; 
      }
      case "ROLE_ADMIN":{
        this.role = "Administrator"
        break;
      }
      default:{
         this.role = "No role"
      }
   }
}

close(){
  this.router.navigate(['/playlists-dashboard']);
}

}
