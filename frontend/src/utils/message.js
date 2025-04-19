import { Message } from 'element-ui';

export const showSuccess = (msg) => {
  Message.success({
    message: msg,
    duration: 500
  });
};

export const showError = (msg) => {
  Message.error({
    message: msg,
    duration: 1500  // 错误提示可以稍长一些
  });
};