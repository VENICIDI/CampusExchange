<template>
  <div class="about-container">
    <div class="about-content">
      <h1>关于校园二手交易平台</h1>
      
      <div class="about-section">
        <h2>我们的使命</h2>
        <p>校园二手交易平台致力于为校园师生提供一个安全、便捷的二手物品交易环境，促进资源循环利用，实现可持续发展理念。</p>
      </div>
      
      <div class="about-section">
        <h2>平台优势</h2>
        <ul>
          <li><strong>安全可靠</strong> - 实名认证，校内交易，降低风险</li>
          <li><strong>便捷高效</strong> - 简单操作，快速发布，当面交易</li>
          <li><strong>物美价廉</strong> - 二手价格，一手品质，为你的校园生活省钱</li>
          <li><strong>环保节约</strong> - 资源再利用，减少浪费，保护环境</li>
        </ul>
      </div>
      
      <div class="about-section">
        <h2>联系我们</h2>
        <p>如有任何问题、建议或投诉，请联系我们：</p>
        <p>邮箱：support@campus-exchange.com</p>
        <p>客服电话：123-4567-8910</p>
      </div>
      
      <div class="about-section">
        <h2>平台准则</h2>
        <p>我们致力于打造健康、诚信的交易环境，请遵守以下原则：</p>
        <ul>
          <li>禁止发布违禁物品、非法物品</li>
          <li>信息真实可靠，不得虚假宣传</li>
          <li>文明交易，友善沟通</li>
          <li>遵守校规校纪和国家法律法规</li>
        </ul>
      </div>
    </div>
  </div>
</template>

<style scoped>
.about-container {
  padding: 60px 20px;
  background: linear-gradient(120deg, #f0f4ff 0%, #f8f9fa 100%);
  min-height: calc(100vh - 170px);
  display: flex;
  justify-content: center;
  width: 100%;
  position: relative;
}

.about-container::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="100" height="100" viewBox="0 0 100 100"><circle cx="50" cy="50" r="1.5" fill="%234a6ee015"/></svg>');
  background-size: 50px 50px;
  opacity: 0.7;
}

.about-content {
  max-width: 800px;
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);
  padding: 40px;
  position: relative;
  animation: fadeIn 0.6s ease-out;
  transition: all 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.about-content:hover {
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.12);
}

h1 {
  color: #4a6ee0;
  margin-bottom: 40px;
  text-align: center;
  font-size: 32px;
  font-weight: 700;
  position: relative;
  padding-bottom: 15px;
}

h1::after {
  content: "";
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 60px;
  height: 3px;
  background: linear-gradient(90deg, #4a6ee0, #6a8fff);
  border-radius: 3px;
}

.about-section {
  margin-bottom: 35px;
  animation: fadeIn 0.8s ease-out;
  animation-fill-mode: both;
}

.about-section:nth-child(2) { animation-delay: 0.1s; }
.about-section:nth-child(3) { animation-delay: 0.2s; }
.about-section:nth-child(4) { animation-delay: 0.3s; }
.about-section:nth-child(5) { animation-delay: 0.4s; }

h2 {
  color: #333;
  font-size: 1.6rem;
  margin-bottom: 18px;
  border-bottom: 1px solid #e6ecff;
  padding-bottom: 12px;
  font-weight: 600;
  position: relative;
}

h2::before {
  content: "•";
  color: #4a6ee0;
  display: inline-block;
  margin-right: 8px;
  font-size: 1.8rem;
  line-height: 0;
  position: relative;
  top: 4px;
}

p {
  line-height: 1.7;
  color: #555;
  margin-bottom: 18px;
  font-size: 16px;
}

ul {
  padding-left: 20px;
  margin-bottom: 20px;
}

li {
  margin-bottom: 12px;
  line-height: 1.6;
  color: #555;
  position: relative;
  padding-left: 8px;
}

li::before {
  content: "•";
  color: #4a6ee0;
  font-weight: bold;
  display: inline-block;
  width: 1em;
  margin-left: -1em;
}

strong {
  color: #333;
  font-weight: 600;
}

@media (max-width: 768px) {
  .about-content {
    padding: 30px 25px;
  }
  
  h1 {
    font-size: 28px;
    margin-bottom: 30px;
  }
  
  h2 {
    font-size: 1.4rem;
  }
}

@media (max-width: 480px) {
  .about-container {
    padding: 40px 15px;
  }
  
  .about-content {
    padding: 25px 20px;
  }
  
  h1 {
    font-size: 24px;
  }
  
  h2 {
    font-size: 1.2rem;
  }
}
</style>
