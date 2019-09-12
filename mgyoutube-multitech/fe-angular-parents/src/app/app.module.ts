import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppComponent } from './app.component';
import { LoginPanel } from './login.panel';
import { LogoutPanel } from './logout.panel';

@NgModule({
  declarations: [
    AppComponent,
    LoginPanel,
    LogoutPanel
  ],
  imports: [
    BrowserModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
