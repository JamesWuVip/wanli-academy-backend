import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GeneratePasswordHash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "password123";
        String hash = encoder.encode(password);
        
        System.out.println("原始密码: " + password);
        System.out.println("新的密码哈希: " + hash);
        System.out.println("验证结果: " + encoder.matches(password, hash));
        
        // 测试与现有哈希的兼容性
        String existingHash = "$2b$10$aFPC5njXck.YuK8OafL6qOGQLgazzXwo011.jkf894HNqwT8fco/i";
        System.out.println("与现有$2b$哈希的兼容性: " + encoder.matches(password, existingHash));
    }
}