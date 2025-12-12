package ru.skypro.homework.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.mock.web.MockMultipartFile;
import ru.skypro.homework.dto.Ad;
import ru.skypro.homework.dto.Ads;
import ru.skypro.homework.dto.CreateOrUpdateAd;
import ru.skypro.homework.dto.ExtendedAd;
import ru.skypro.homework.model.AdEntity;
import ru.skypro.homework.model.UserEntity;
import ru.skypro.homework.repository.AdRepository;
import ru.skypro.homework.repository.UserRepository;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdServiceTest {

    @Mock
    private AdRepository adRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AdMappingService adMappingService;

    @Mock
    private UserMappingService userMappingService;

    @Mock
    private UserService userService;

    private AdService adService;

    private UserEntity testUser;
    private UserEntity adminUser;
    private AdEntity testAd;
    private Ad testAdDto;
    private ExtendedAd testExtendedAdDto;
    private CreateOrUpdateAd updateDto;

    @BeforeEach
    void setUp() {
        adService = new AdService(adRepository, userRepository, adMappingService, userMappingService, userService);

        testUser = UserEntity.builder().id(1L).email("user@example.com").role("USER").build();
        adminUser = UserEntity.builder().id(99L).email("admin@example.com").role("ADMIN").build();
        testAd = AdEntity.builder().id(10L).title("Test Ad").price(100).author(testUser).build();
        testAdDto = new Ad();
        testAdDto.setPk(10);
        testExtendedAdDto = new ExtendedAd();
        testExtendedAdDto.setPk(10);
        updateDto = new CreateOrUpdateAd();
        updateDto.setTitle("Updated Title");
        updateDto.setPrice(200);
    }

    // --- getAllAds ---

    @Test
    void getAllAds_ReturnsAllAds() {

        List<AdEntity> entities = List.of(testAd);
        List<Ad> dtos = List.of(testAdDto);
        when(adRepository.findAll()).thenReturn(entities);
        when(adMappingService.toAdDto(testAd)).thenReturn(testAdDto);


        Ads result = adService.getAllAds();


        assertEquals(1, result.getCount());
        assertEquals(dtos, result.getResults());
        verify(adRepository).findAll();
        verify(adMappingService).toAdDto(testAd);
    }

    // --- getMyAds ---

    @Test
    void getMyAds_WithAuthenticatedUser_ReturnsUserAds() {

        List<AdEntity> entities = List.of(testAd);
        List<Ad> dtos = List.of(testAdDto);
        when(userService.getCurrentUserEntity()).thenReturn(testUser);
        when(adRepository.findByAuthor(testUser)).thenReturn(entities);
        when(adMappingService.toAdDto(testAd)).thenReturn(testAdDto);


        Ads result = adService.getMyAds();


        assertEquals(1, result.getCount());
        assertEquals(dtos, result.getResults());
        verify(userService).getCurrentUserEntity();
        verify(adRepository).findByAuthor(testUser);
        verify(adMappingService).toAdDto(testAd);
    }

    // --- getAdById ---

    @Test
    void getAdById_WithValidId_ReturnsExtendedAd() {

        when(adRepository.findById(10L)).thenReturn(Optional.of(testAd));
        when(adMappingService.toExtendedDto(testAd)).thenReturn(testExtendedAdDto);


        ExtendedAd result = adService.getAdById(10L);


        assertEquals(testExtendedAdDto, result);
        verify(adRepository).findById(10L);
        verify(adMappingService).toExtendedDto(testAd);
    }

    @Test
    void getAdById_WithInvalidId_ThrowsRuntimeException() {

        when(adRepository.findById(999L)).thenReturn(Optional.empty());


        RuntimeException thrown = assertThrows(RuntimeException.class, () -> adService.getAdById(999L));
        assertEquals("Ad not found", thrown.getMessage());
        verify(adRepository).findById(999L);
    }

    // --- createAd ---

    @Test
    void createAd_WithValidData_CreatesAndReturnsAd() throws IOException {

        String uploadDir = "test_uploads";
        String newImageName = "new_ad_image.jpg";
        byte[] imageContent = "fake_image_content".getBytes();
        MockMultipartFile mockFile = new MockMultipartFile("image", newImageName, "image/jpeg", imageContent);

        CreateOrUpdateAd createDto = new CreateOrUpdateAd();
        createDto.setTitle("New Ad");
        createDto.setPrice(50);

        AdEntity newAdEntity = AdEntity.builder().title("New Ad").price(50).author(testUser).build();
        Ad createdAdDto = new Ad(); // предположим DTO создан

        when(userService.getCurrentUserEntity()).thenReturn(testUser);
        when(adMappingService.toNewEntity(createDto, testUser)).thenReturn(newAdEntity);
        when(adRepository.save(newAdEntity)).thenReturn(testAd);
        when(adMappingService.toAdDto(testAd)).thenReturn(testAdDto);


        Path uploadPath = Paths.get(uploadDir);
        Files.createDirectories(uploadPath);


        adService.adImageUploadPath = uploadDir;
        Ad result = assertDoesNotThrow(() -> adService.createAd(createDto, mockFile));


        assertEquals(testAdDto, result);
        verify(userService).getCurrentUserEntity();
        verify(adMappingService).toNewEntity(createDto, testUser);
        verify(adRepository).save(newAdEntity);
        verify(adMappingService).toAdDto(testAd);
        assertNotNull(newAdEntity.getImage());


        Files.walk(uploadPath)
                .filter(Files::isRegularFile)
                .forEach(path -> {
                    try { Files.delete(path); } catch (IOException e) { /* ignore */ }
                });
        Files.delete(uploadPath);
    }

    // --- updateAd ---

    @Test
    void updateAd_WithOwnerPermission_UpdatesAndReturnsAd() {

        when(adRepository.findById(10L)).thenReturn(Optional.of(testAd));
        doNothing().when(userService).checkOwnerOrAdmin(testUser);
        when(adMappingService.updateEntity(updateDto, testAd)).Return();
        when(adRepository.save(testAd)).thenReturn(testAd);
        when(adMappingService.toAdDto(testAd)).thenReturn(testAdDto);


        Ad result = adService.updateAd(10L, updateDto);


        assertEquals(testAdDto, result);
        verify(adRepository).findById(10L);
        verify(userService).checkOwnerOrAdmin(testUser); // Проверка вызова
        verify(adMappingService).updateEntity(updateDto, testAd);
        verify(adRepository).save(testAd);
        verify(adMappingService).toAdDto(testAd);
    }

    @Test
    void updateAd_WithAdminPermission_UpdatesAndReturnsAd() {

        UserEntity otherAuthor = UserEntity.builder().id(2L).email("other@example.com").build();
        AdEntity adByOther = AdEntity.builder().id(10L).title("Ad by Other").author(otherAuthor).build();

        when(adRepository.findById(10L)).thenReturn(Optional.of(adByOther));
        doNothing().when(userService).checkOwnerOrAdmin(otherAuthor); // Мокаем проверку прав, как будто текущий - админ
        when(adMappingService.updateEntity(updateDto, adByOther)).Return();
        when(adRepository.save(adByOther)).thenReturn(adByOther);
        when(adMappingService.toAdDto(adByOther)).thenReturn(testAdDto);


        Ad result = adService.updateAd(10L, updateDto);


        assertEquals(testAdDto, result);
        verify(adRepository).findById(10L);
        verify(userService).checkOwnerOrAdmin(otherAuthor); // Проверка вызова
        verify(adMappingService).updateEntity(updateDto, adByOther);
        verify(adRepository).save(adByOther);
        verify(adMappingService).toAdDto(adByOther);
    }

    @Test
    void updateAd_WithoutPermission_ThrowsRuntimeException() {

        UserEntity otherAuthor = UserEntity.builder().id(2L).email("other@example.com").build();
        AdEntity adByOther = AdEntity.builder().id(10L).title("Ad by Other").author(otherAuthor).build();

        when(adRepository.findById(10L)).thenReturn(Optional.of(adByOther));
        doThrow(new RuntimeException("You do not have permission to perform this action."))
                .when(userService).checkOwnerOrAdmin(otherAuthor); // Мокаем проверку прав, чтобы выбросить исключение


        RuntimeException thrown = assertThrows(RuntimeException.class, () -> adService.updateAd(10L, updateDto));
        assertEquals("You do not have permission to perform this action.", thrown.getMessage());
        verify(adRepository).findById(10L);
        verify(userService).checkOwnerOrAdmin(otherAuthor);
        verify(adRepository, never()).save(any()); // Убедимся, что сохранение не произошло
    }

    @Test
    void updateAd_WithInvalidId_ThrowsRuntimeException() {

        when(adRepository.findById(999L)).thenReturn(Optional.empty());


        RuntimeException thrown = assertThrows(RuntimeException.class, () -> adService.updateAd(999L, updateDto));
        assertEquals("Ad not found", thrown.getMessage());
        verify(adRepository).findById(999L);
        verify(userService, never()).checkOwnerOrAdmin(any()); // Проверка не должна вызываться
    }

    // --- deleteAd ---

    @Test
    void deleteAd_WithOwnerPermission_DeletesAd() throws IOException {

        String imagePath = "uploads/ads/test_image.jpg";
        AdEntity adToDelete = AdEntity.builder().id(10L).title("Ad to Delete").author(testUser).image(imagePath).build();

        when(adRepository.findById(10L)).thenReturn(Optional.of(adToDelete));
        doNothing().when(userService).checkOwnerOrAdmin(testUser); // Мокаем проверку прав

        // Act
        assertDoesNotThrow(() -> adService.deleteAd(10L));

        // Assert
        verify(adRepository).findById(10L);
        verify(userService).checkOwnerOrAdmin(testUser); // Проверка вызова
        verify(adRepository).delete(adToDelete);
        // Проверка удаления файла через Files.deleteIfExists - сложно мокировать статический метод
        // В реальном приложении, как и с аватарами, логику работы с файлами можно вынести в отдельный сервис для лучшего тестирования.
    }

    @Test
    void deleteAd_WithAdminPermission_DeletesAd() throws IOException {
        // Arrange
        UserEntity otherAuthor = UserEntity.builder().id(2L).email("other@example.com").build();
        String imagePath = "uploads/ads/test_image.jpg";
        AdEntity adToDelete = AdEntity.builder().id(10L).title("Ad to Delete").author(otherAuthor).image(imagePath).build();

        when(adRepository.findById(10L)).thenReturn(Optional.of(adToDelete));
        doNothing().when(userService).checkOwnerOrAdmin(otherAuthor); // Мокаем проверку прав, как будто текущий - админ

        // Act
        assertDoesNotThrow(() -> adService.deleteAd(10L));

        // Assert
        verify(adRepository).findById(10L);
        verify(userService).checkOwnerOrAdmin(otherAuthor); // Проверка вызова
        verify(adRepository).delete(adToDelete);
    }

    @Test
    void deleteAd_WithoutPermission_ThrowsRuntimeException() {
        // Arrange
        UserEntity otherAuthor = UserEntity.builder().id(2L).email("other@example.com").build();
        AdEntity adToDelete = AdEntity.builder().id(10L).title("Ad to Delete").author(otherAuthor).build();

        when(adRepository.findById(10L)).thenReturn(Optional.of(adToDelete));
        doThrow(new RuntimeException("You do not have permission to perform this action."))
                .when(userService).checkOwnerOrAdmin(otherAuthor); // Мокаем проверку прав, чтобы выбросить исключение

        // Act & Assert
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> adService.deleteAd(10L));
        assertEquals("You do not have permission to perform this action.", thrown.getMessage());
        verify(adRepository).findById(10L);
        verify(userService).checkOwnerOrAdmin(otherAuthor);
        verify(adRepository, never()).delete(any()); // Убедимся, что удаление не произошло
    }

    @Test
    void deleteAd_WithInvalidId_ThrowsRuntimeException() {
        // Arrange
        when(adRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> adService.deleteAd(999L));
        assertEquals("Ad not found", thrown.getMessage());
        verify(adRepository).findById(999L);
        verify(userService, never()).checkOwnerOrAdmin(any()); // Проверка не должна вызываться
    }

    // --- updateAdImage ---

    @Test
    void updateAdImage_WithOwnerPermission_UpdatesImage() throws IOException {
        // Arrange
        String oldImagePath = "uploads/ads/old_image.jpg";
        AdEntity adToUpdate = AdEntity.builder().id(10L).title("Ad to Update").author(testUser).image(oldImagePath).build();

        String uploadDir = "test_uploads";
        String newImageName = "new_ad_image.jpg";
        byte[] imageContent = "fake_image_content".getBytes();
        MockMultipartFile mockFile = new MockMultipartFile("image", newImageName, "image/jpeg", imageContent);

        when(adRepository.findById(10L)).thenReturn(Optional.of(adToUpdate));
        doNothing().when(userService).checkOwnerOrAdmin(testUser); // Мокаем проверку прав

        // Создаем директорию для теста
        Path uploadPath = Paths.get(uploadDir);
        Files.createDirectories(uploadPath);

        // Act
        adService.adImageUploadPath = uploadDir; // Изменяем путь для теста
        assertDoesNotThrow(() -> adService.updateAdImage(10L, mockFile));

        // Assert
        verify(adRepository).findById(10L);
        verify(userService).checkOwnerOrAdmin(testUser); // Проверка вызова
        verify(adRepository).save(adToUpdate);
        assertNotNull(adToUpdate.getImage()); // Убедимся, что image был обновлен
        assertNotEquals(oldImagePath, adToUpdate.getImage()); // Убедимся, что путь изменился

        // Очищаем тестовую директорию
        Files.walk(uploadPath)
                .filter(Files::isRegularFile)
                .forEach(path -> {
                    try { Files.delete(path); } catch (IOException e) { /* ignore */ }
                });
        Files.delete(uploadPath);
    }

    @Test
    void updateAdImage_WithAdminPermission_UpdatesImage() throws IOException {
        // Arrange
        UserEntity otherAuthor = UserEntity.builder().id(2L).email("other@example.com").build();
        String oldImagePath = "uploads/ads/old_image.jpg";
        AdEntity adToUpdate = AdEntity.builder().id(10L).title("Ad to Update").author(otherAuthor).image(oldImagePath).build();

        String uploadDir = "test_uploads";
        String newImageName = "new_ad_image.jpg";
        byte[] imageContent = "fake_image_content".getBytes();
        MockMultipartFile mockFile = new MockMultipartFile("image", newImageName, "image/jpeg", imageContent);

        when(adRepository.findById(10L)).thenReturn(Optional.of(adToUpdate));
        doNothing().when(userService).checkOwnerOrAdmin(otherAuthor); // Мокаем проверку прав, как будто текущий - админ

        // Создаем директорию для теста
        Path uploadPath = Paths.get(uploadDir);
        Files.createDirectories(uploadPath);

        // Act
        adService.adImageUploadPath = uploadDir; // Изменяем путь для теста
        assertDoesNotThrow(() -> adService.updateAdImage(10L, mockFile));

        // Assert
        verify(adRepository).findById(10L);
        verify(userService).checkOwnerOrAdmin(otherAuthor); // Проверка вызова
        verify(adRepository).save(adToUpdate);
        assertNotNull(adToUpdate.getImage()); // Убедимся, что image был обновлен
        assertNotEquals(oldImagePath, adToUpdate.getImage()); // Убедимся, что путь изменился

        // Очищаем тестовую директорию
        Files.walk(uploadPath)
                .filter(Files::isRegularFile)
                .forEach(path -> {
                    try { Files.delete(path); } catch (IOException e) { /* ignore */ }
                });
        Files.delete(uploadPath);
    }

    @Test
    void updateAdImage_WithoutPermission_ThrowsRuntimeException() throws IOException {
        // Arrange
        UserEntity otherAuthor = UserEntity.builder().id(2L).email("other@example.com").build();
        String oldImagePath = "uploads/ads/old_image.jpg";
        AdEntity adToUpdate = AdEntity.builder().id(10L).title("Ad to Update").author(otherAuthor).image(oldImagePath).build();

        String newImageName = "new_ad_image.jpg";
        byte[] imageContent = "fake_image_content".getBytes();
        MockMultipartFile mockFile = new MockMultipartFile("image", newImageName, "image/jpeg", imageContent);

        when(adRepository.findById(10L)).thenReturn(Optional.of(adToUpdate));
        doThrow(new RuntimeException("You do not have permission to perform this action."))
                .when(userService).checkOwnerOrAdmin(otherAuthor); // Мокаем проверку прав, чтобы выбросить исключение

        // Act & Assert
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> adService.updateAdImage(10L, mockFile));
        assertEquals("You do not have permission to perform this action.", thrown.getMessage());
        verify(adRepository).findById(10L);
        verify(userService).checkOwnerOrAdmin(otherAuthor);
        verify(adRepository, never()).save(any()); // Убедимся, что сохранение не произошло
    }

    @Test
    void updateAdImage_WithInvalidId_ThrowsRuntimeException() throws IOException {
        // Arrange
        String newImageName = "new_ad_image.jpg";
        byte[] imageContent = "fake_image_content".getBytes();
        MockMultipartFile mockFile = new MockMultipartFile("image", newImageName, "image/jpeg", imageContent);

        when(adRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> adService.updateAdImage(999L, mockFile));
        assertEquals("Ad not found", thrown.getMessage());
        verify(adRepository).findById(999L);
        verify(userService, never()).checkOwnerOrAdmin(any()); // Проверка не должна вызываться
    }
}