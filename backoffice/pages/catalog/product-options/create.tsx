import type { NextPage } from 'next';
import React from 'react';
import Link from 'next/link';
import { useForm } from 'react-hook-form';
import { createProductOption } from '../../../modules/catalog/services/ProductOptionService';
import { ProductOption } from '../../../modules/catalog/models/ProductOption';
import { useRouter } from 'next/router';
import { PRODUCT_OPTIONS_URL } from '../../../constants/Common';
import { useCreatingContext } from '../../../common/hooks/UseToastContext';
import CustomToast from '../../../common/items/CustomToast';

const ProductOptionCreate: NextPage = () => {
  const { toastVariant, toastHeader, showToast, setShowToast, handleCreatingResponse } =
    useCreatingContext();
  const router = useRouter();
  const { register, handleSubmit, formState } = useForm();
  const { errors } = formState;
  const handleSubmitOption = async (event: any) => {
    let productOption: ProductOption = {
      id: 0,
      name: event.name,
    };
    let response = await createProductOption(productOption);
    handleCreatingResponse(response, PRODUCT_OPTIONS_URL);
  };

  return (
    <>
      <div className="row mt-5">
        <div className="col-md-8">
          <h2>Create Product Option</h2>
          <form onSubmit={handleSubmit(handleSubmitOption)}>
            <div className="mb-3">
              <div className="mb-3">
                <label className="form-label">Name</label>
                <input
                  className="form-control"
                  {...register('name', { required: true })}
                  type="text"
                  id="name"
                  name="name"
                />
                {errors.name && errors.name.type == 'required' && (
                  <p className="text-danger">Please enter the name product option</p>
                )}
              </div>
            </div>
            <button className="btn btn-primary" type="submit">
              Save
            </button>
            &emsp;
            <Link href="/catalog/product-options">
              <button className="btn btn-outline-secondary">Cancel</button>
            </Link>
          </form>
        </div>
      </div>
      {showToast && (
        <CustomToast
          variant={toastVariant}
          header={toastHeader}
          show={showToast}
          setShow={setShowToast}
        ></CustomToast>
      )}
    </>
  );
};

export default ProductOptionCreate;
