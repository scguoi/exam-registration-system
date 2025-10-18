#!/usr/bin/env python3
"""
BCrypt 密码验证脚本
用于验证数据库中的密码哈希对应哪个明文密码
"""

import sys

try:
    import bcrypt
except ImportError:
    print("需要安装 bcrypt 库：pip3 install bcrypt")
    sys.exit(1)

# 数据库中的密码哈希
hashes = {
    "原始 admin 密码 (init.sql)": "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi",
    "当前 admin 密码 (已更新)": "$2a$10$YTR9b1V8emCUy8WkpT5.QuZLSqnGx3rToQ0p8KMmr0x1MmxAODQ8u",
    "测试考生密码 (13800138000)": "$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr/w0wTRaI5p2S5K.",
}

# 要测试的密码
passwords_to_test = ["password", "admin123", "123456"]

print("=" * 80)
print("BCrypt 密码验证")
print("=" * 80)

# 验证现有哈希
for hash_name, hash_value in hashes.items():
    print(f"\n{hash_name}:")
    print(f"哈希: {hash_value}")
    for password in passwords_to_test:
        try:
            if bcrypt.checkpw(password.encode('utf-8'), hash_value.encode('utf-8')):
                print(f"  ✓ {password}: 匹配")
            else:
                print(f"  ✗ {password}: 不匹配")
        except Exception as e:
            print(f"  ✗ {password}: 验证失败 - {e}")

# 生成新的密码哈希
print("\n" + "=" * 80)
print("生成新的密码哈希 (strength=10)")
print("=" * 80)

for password in passwords_to_test:
    salt = bcrypt.gensalt(rounds=10)
    hashed = bcrypt.hashpw(password.encode('utf-8'), salt)
    print(f"\n密码: {password}")
    print(f"哈希: {hashed.decode('utf-8')}")

    # 验证生成的哈希
    if bcrypt.checkpw(password.encode('utf-8'), hashed):
        print("验证: ✓ 通过")
    else:
        print("验证: ✗ 失败")

print("\n" + "=" * 80)
