import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppComponent } from './app.component';
import { LoginPanel } from './login.panel';
import { LogoutPanel } from './logout.panel';
import { ChildrenPanel } from './children.panel';
import { AddChildrenPanel } from './add.children.panel';
import { SearchPanel } from './search.panel';
import { PlayerPanel } from './player.panel';

@NgModule({
  declarations: [
    AppComponent,
    LoginPanel,
    LogoutPanel,
    ChildrenPanel,
    AddChildrenPanel,
    SearchPanel,
    PlayerPanel
  ],
  imports: [
    BrowserModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
