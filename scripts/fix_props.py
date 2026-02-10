#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""临时脚本：修复 Props 接口添加 skipScanThreshold"""

import re

path = r'd:\32516\Desktop\project\sushe_web_2025\manager\src\components\core\forms\art-import-dialog\index.vue'

# 尝试多种编码读取
for enc in ['utf-8', 'utf-8-sig', 'gbk', 'gb2312', 'cp1252']:
    try:
        with open(path, 'r', encoding=enc) as f:
            content = f.read()
        # 验证是否正确读取中文
        if '扫描函数' in content:
            print(f'Using encoding: {enc}')
            break
    except:
        continue
else:
    print('Failed to read file with any encoding')
    exit(1)

# 使用正则表达式匹配，更灵活
pattern = r'(chunkUploadThreshold\?: number\s*\n\s*)(\/\*\* 扫描函数)'
replacement = r'''\1/** 跳过前端扫描的文件大小阈值（字节），超过此大小直接由后端校验，默认 50MB */
    skipScanThreshold?: number
    \2'''

if re.search(pattern, content):
    content = re.sub(pattern, replacement, content)
    with open(path, 'w', encoding=enc) as f:
        f.write(content)
    print('Success: skipScanThreshold added to Props interface')
else:
    print('Pattern not found with regex')
    # 查找相关内容
    idx = content.find('chunkUploadThreshold')
    if idx >= 0:
        print('Context around chunkUploadThreshold:')
        print(content[idx:idx+200])
