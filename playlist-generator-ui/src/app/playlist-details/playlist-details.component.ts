import { Component, OnInit, Input } from '@angular/core';
import { Playlist } from '../models/playlist';
import { ActivatedRoute, Router } from '@angular/router';
import { PlaylistService } from '../services/playlist.service';
import { DomSanitizer } from '@angular/platform-browser';
import { User } from '../models/user';
import { AuthenticationService } from '../services/authentication.service';
import { ConfirmationService } from '../confirmation-dialog/confirmation.service';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';

@Component({
  selector: 'playlist-details',
  templateUrl: './playlist-details.component.html',
  styleUrls: ['./playlist-details.component.scss']
})
export class PlaylistDetailsComponent implements OnInit {
  
  edditing: boolean;
  playUrl: boolean;
  loading: boolean;
  editDeleteButtonsDisabled: boolean;
  previewUrl: string;
  titleForm: FormGroup;
  loggedUser: User;
  playlist: Playlist;
  playlistId: number;

  constructor(private formBuilder: FormBuilder, private route: ActivatedRoute, private playlistService: PlaylistService, 
    private domSanitizer: DomSanitizer, private router: Router, private authenticationService: AuthenticationService,
    private confirmationService: ConfirmationService) { 
      this.authenticationService.currentUser.subscribe(currentUser => this.loggedUser = currentUser);
      this.edditing = false;
      this.playUrl = false;
      this.loading = true;
      this.editDeleteButtonsDisabled = true;
    }

  ngOnInit() {
    if(this.route.snapshot.paramMap.has("playlistId")){
       this.playlistId = +this.route.snapshot.paramMap.get("playlistId");
       this.displayPlaylist(this.playlistId);
    }
    this.titleForm = this.formBuilder.group({
      title: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(10)]]
  });
  }

  displayPlaylist(playlistId: number){
  this.playlistService.getPlaylist(playlistId).subscribe(data => {
      this.playlist = data;
   },error => {
    alert("Error: " +error);
    },() => {
        if(!this.loggedUser) {
          this.editDeleteButtonsDisabled = true;
        }
        else if(this.playlist.username === this.loggedUser.username || this.loggedUser.role === 'ROLE_ADMIN'){
          this.editDeleteButtonsDisabled = false;
        }
        this.loading = !this.loading;
     });
  }

  get field() { return this.titleForm.controls; }

  onSubmit(event) {

    this.edditing = !this.edditing;
    if (this.titleForm.invalid) {
        return;
    }
    if(!this.edditing){
      this.playlistService.editPlaylist(event.value.title, this.playlistId, this.loggedUser.username).subscribe(data => {
      },error => {
         alert("Error: " +error);
       },() => { 
          this.playlist.title = event.value.title;
       });
    }
    
}

play(value: string){
  this.playUrl = !this.playUrl;
  this.previewUrl = value;
}

handelStop(){
  this.playUrl = !this.playUrl;
}

deletePlaylist(){
  
  if(this.edditing){
    this.edditing = !this.edditing;
  }
  else{
    this.openConfirmationDialog();
  }
}

openConfirmationDialog() {
  this.confirmationService.confirm('Please confirm!', 'Do you really want to delete this playlist?')
  .then((confirmed) => {
    
    if(confirmed){
      this.playlistService.deletePlaylist(this.playlistId).subscribe(data => {
        
      },error => {
        alert("Error: " +error);
      },() => { 
        this.playlistService.deletePlaylist(this.playlistId);
        this.router.navigate(['/playlists-dashboard']);
      });
    }
  })
  .catch(() => console.log('Dialog closed'));
}

close(){
  this.router.navigate(['/playlists-dashboard']);
}

}
