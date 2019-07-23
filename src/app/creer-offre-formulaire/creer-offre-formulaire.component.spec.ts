import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CreerOffreFormulaireComponent } from './creer-offre-formulaire.component';

describe('CreerOffreFormulaireComponent', () => {
  let component: CreerOffreFormulaireComponent;
  let fixture: ComponentFixture<CreerOffreFormulaireComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CreerOffreFormulaireComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CreerOffreFormulaireComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
