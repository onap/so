import { Component, OnInit } from '@angular/core';
import { DataService } from '../data.service';
import { NgxSpinnerService } from 'ngx-spinner';
import { FormGroup, FormControl, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { ToastrNotificationService } from '../toastr-notification-service.service';

@Component({
  selector: 'app-onboard',
  templateUrl: './onboard.component.html',
  styleUrls: ['./onboard.component.scss']
})
export class OnboardComponent implements OnInit {

  constructor(private data: DataService, private spinner: NgxSpinnerService, private http: HttpClient,
    private popup: ToastrNotificationService) { }

  fileName: string;
  typeData = [
    "Service",
    "VNF",
    "Network"
  ];
  saveDataObj = {};
  isChecked:boolean = false;
  fileList = [];
  modelTypes: string[] = [
    'Service',
    'VNF',
    'NetWork',
  ];
  myform: FormGroup;
  modelName: FormControl;
  modelVersionId: FormControl;
  operation: FormControl;
  orchestrationFlow: FormControl;
  modelType: FormControl;


  ngOnInit() {
    this.createFormControls();
    this.createForm();
  }

  createFormControls() {
    this.modelName = new FormControl('', );
    this.modelVersionId = new FormControl('', );
    this.operation = new FormControl('', [
      Validators.required
    ]);
    this.orchestrationFlow = new FormControl('',);
    this.modelType = new FormControl('');
  }

  createForm() {
    this.myform = new FormGroup({
      modelName: this.modelName,
      modelVersionId: this.modelVersionId,
      operation: this.operation,
      orchestrationFlow: this.orchestrationFlow,
      modelType: this.modelType
    });
  }

  onSubmit() {
    if (this.myform.valid && this.isChecked) {
      console.log("Form Submitted!");
      console.log("formdata", this.myform.value)
      let data = this.myform.value;
      this.saveServiceRecipes(JSON.stringify(data));
      this.myform.reset();
    } else if(this.fileList.length > 0) {
      this.handleUpload();
    } else {
      this.popup.error("Please fill valid data.");
    }
  }

  beforeUpload = (evt: any): boolean => {
    this.fileList = [];
    if(evt) {
      let file = evt.currentTarget.files[0];
      if(file.name.includes(".war")) {
        this.fileName = file.name;
        this.fileList = this.fileList.concat(file);
      } else {
        this.popup.error("Invalid file format.");
      }
    }
    return false;
  };

  saveServiceRecipes(data: any): void {
    this.data.saveServiceRecipe(data)
      .subscribe((data: any) => {
        console.log(JSON.stringify(data));
        if(data != null) {
          if (data.id && data.id != "") {
            this.popup.info("Data stored in database.");
          } else if(data.errMsg) {
            this.popup.error(data.errMsg);
          }
        }
        this.spinner.hide();
      },error => {
        console.log(error);
        this.popup.error("Unable to store bpmn data, Error code:" + error.status);
        this.spinner.hide();
    });
  }

  handleUpload(): void {
    if (this.fileList.length == 0) {
      return;
    }
    this.spinner.show()
    const formData = new FormData();
    this.fileList.forEach((file: any) => {
      formData.append('file', file, file.name);
    });
    this.data.onboardBPMNInfra(formData)
      .subscribe((data: any) => {
        this.spinner.hide();
        console.log(JSON.stringify(data));
        if(data != null) {
          if(data.result == "true") {
            this.popup.info(data.message);
          } else if(data.errMsg) {
            this.popup.error(data.errMsg);
          } else {
            this.popup.error(data.message);
          }
        }
      },error => {
        console.log(error);
        this.popup.error("Unable to upload bpmn file, Error code:" + error.status);
        this.spinner.hide();
    });
  }

  checkDB () {
    this.isChecked = this.isChecked ? false : true;
  }
}

