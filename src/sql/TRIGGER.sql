CREATE TRIGGER sync_data_to_add AFTER INSERT ON t_ems_instrument FOR EACH ROW
BEGIN
INSERT INTO ERP_69.test_instrument (
id,
code,
name,
model,
manufacturer,
serial_number,
price,
appraisal_date,
`range`,
`level`,
used,
device_admin,
use_dept,
calibration_param,
store_place,
expire_date,
calibration_number,
purchase_date,
affirm_way,
calibration_corporation,
incorporated_date,
check_price,
status,
create_time,
update_time,
device_state
)
VALUES
(
NEW.id,
NEW.equipment_code,
NEW.equipment_name,
NEW.equipment_model,
NEW.manufacturer,
NEW.serial_number,
NEW.original_price,
NEW.appraisal_date,
NEW.equipment_range,
NEW.equipment_level,
NEW.user,
NEW.user_id,
NEW.use_dept,
NEW.calibration_param,
NEW.store_place,
NEW.expire_date,
NEW.calibration_number,
NEW.purchase_date,
NEW.affirm_way,
NEW.calibration_corporation,
NEW.accept_date,
NEW.check_price,
NEW.available,
NEW.create_time,
NEW.update_time,
NEW.status
) ;
END ;


CREATE TRIGGER sync_data_update_to_update AFTER UPDATE ON t_ems_instrument FOR EACH ROW
BEGIN
	UPDATE ERP_69.test_instrument
SET `code` = NEW.equipment_code,
 `name` = NEW.equipment_name,
 model = NEW.equipment_model,
 manufacturer = NEW.manufacturer,
 serial_number = NEW.serial_number,
 price = NEW.original_price,
 appraisal_date = NEW.appraisal_date,
 `range` = NEW.equipment_range,
 `level` = NEW.equipment_level,
 used = NEW.`user`,
 device_admin = NEW.user_id,
 use_dept = NEW.use_dept,
 calibration_param = NEW.calibration_param,
 store_place = NEW.store_place,
 expire_date = NEW.expire_date,
 calibration_number = NEW.calibration_number,
 purchase_date = NEW.purchase_date,
 affirm_way = NEW.affirm_way,
 calibration_corporation = NEW.calibration_corporation,
 incorporated_date = NEW.accept_date,
 check_price = NEW.check_price,
 `status` = NEW.available,
 create_time = NEW.create_time,
 update_time = NEW.update_time,
 device_state = NEW.`status`
WHERE
	id = NEW.id;
END;


CREATE TRIGGER sync_data_delete_to_delete
AFTER DELETE ON t_ems_instrument
FOR EACH ROW
BEGIN
    DELETE FROM ERP_69.test_instrument WHERE id=OLD.id;
END;





