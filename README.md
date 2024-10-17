# 2024小挑相关（看图写话课堂教学辅助系统

演示: https://pan.baidu.com/s/16E14KlWZrX5qtCBw9E6WtA?pwd=t2u2

缺少训练好的模型，且现有模型训练代码存在大量错误（包括但不限于使用未定义的变量、、、
缺少关键文件uistart.py
代码严重冗余，可读性极差

展望：
1. 优化模型（这个我没能力做
2. 重构后端代码。（ing
   1. 重新设计数据库
   2. 分布式？
   3. ci/cd？
3. 优化用户体验
   1. 界面布局（手机端适配
   2. 长按输入语音松开识别

---
SpeechRecognitionService
   - BaiduVOP
AIAgentService
  - BaiduQianfan
EvaluationService

---
teacher: id login name password
student: id login name password class_id
class_info: id name teacher_id
class_task: id name class_id

