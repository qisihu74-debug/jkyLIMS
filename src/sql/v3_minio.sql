UPDATE home_affiche
SET file_url = REPLACE (
	file_url,
	'http://121.89.242.0:9000',
	'https://minio.lims.design'
);

UPDATE sop_standard_instruction
SET file_url = REPLACE (
	file_url,
	'http://121.89.242.0:9000',
	'https://minio.lims.design'
);

UPDATE sys_csos_img
SET img_url = REPLACE (
	img_url,
	'http://121.89.242.0:9000',
	'https://minio.lims.design'
);

UPDATE sys_oss
SET url = REPLACE (
	url,
	'http://121.89.242.0:9000',
	'https://minio.lims.design'
);

UPDATE t_data_info
SET data_url = REPLACE (
	data_url,
	'http://121.89.242.0:9000',
	'https://minio.lims.design'
),
 data_img_url = REPLACE (
	data_img_url,
	'http://121.89.242.0:9000',
	'https://minio.lims.design'
),
 data_video_url = REPLACE (
	data_video_url,
	'http://121.89.242.0:9000',
	'https://minio.lims.design'
);

UPDATE test_entrust_file_rel
SET file_url = REPLACE (
	file_url,
	'http://121.89.242.0:9000',
	'https://minio.lims.design'
);

UPDATE test_entrusted_sample_checkitem_rel
SET origin_url_pdf = REPLACE (
	origin_url_pdf,
	'http://121.89.242.0:9000',
	'https://minio.lims.design'
),
 origin_url = REPLACE (
	origin_url,
	'http://121.89.242.0:9000',
	'https://minio.lims.design'
);

UPDATE test_entrusted_sample_details_rel
SET product_excel_url = REPLACE (
	product_excel_url,
	'http://121.89.242.0:9000',
	'https://minio.lims.design'
),
 report_edit_url = REPLACE (
	report_edit_url,
	'http://121.89.242.0:9000',
	'https://minio.lims.design'
);

UPDATE test_instrument_appraisal_record
SET file_url = REPLACE (
	file_url,
	'http://121.89.242.0:9000',
	'https://minio.lims.design'
);

UPDATE sys_user
SET signature_url = REPLACE (
	signature_url,
	'http://121.89.242.0:9000',
	'https://minio.lims.design'
);

UPDATE test_original_record_template
SET file_url = REPLACE (
	file_url,
	'http://121.89.242.0:9000',
	'https://minio.lims.design'
);

UPDATE test_report_original_template
SET url = REPLACE (
	url,
	'http://121.89.242.0:9000',
	'https://minio.lims.design'
);

UPDATE test_report_record
SET report_url = REPLACE (
	report_url,
	'http://121.89.242.0:9000',
	'https://minio.lims.design'
),
 seal_report_url = REPLACE (
	seal_report_url,
	'http://121.89.242.0:9000',
	'https://minio.lims.design'
);

UPDATE test_report_record_mid
SET report_url = REPLACE (
	report_url,
	'http://121.89.242.0:9000',
	'https://minio.lims.design'
),
 seal_report_url = REPLACE (
	seal_report_url,
	'http://121.89.242.0:9000',
	'https://minio.lims.design'
);

UPDATE test_report_template
SET report_file_uri = REPLACE (
	report_file_uri,
	'http://121.89.242.0:9000',
	'https://minio.lims.design'
);

UPDATE test_sample_file_rel
SET file_url = REPLACE (
	file_url,
	'http://121.89.242.0:9000',
	'https://minio.lims.design'
);

UPDATE test_standard_file
SET file_url = REPLACE (
	file_url,
	'http://121.89.242.0:9000',
	'https://minio.lims.design'
);
