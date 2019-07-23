import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ListeOffreComponent } from './liste-offre.component';

describe('ListeOffreComponent', () => {
  let component: ListeOffreComponent;
  let fixture: ComponentFixture<ListeOffreComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ListeOffreComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ListeOffreComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
